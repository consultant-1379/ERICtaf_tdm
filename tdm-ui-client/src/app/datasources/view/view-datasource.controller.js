const _state = new WeakMap();
const _scope = new WeakMap();
const _logger = new WeakMap();
const _dataSourceResource = new WeakMap();
const _copyDataSourceModalService = new WeakMap();
const _approvalRequestModalService = new WeakMap();
const _approvalCommentModalService = new WeakMap();
const _historyModalService = new WeakMap();
const _contextResource = new WeakMap();
const _validationService = new WeakMap();

export default class DataSourceViewController {
    constructor(dataSourceIdentity, dataSourceRecords, dataSourceVersions,
                dataSourceResource,
                copyDataSourceModalService,
                approvalRequestModalService,
                approvalCommentModalService,
                approvalStatusConstants, session,
                logger, $state, $scope, $rootScope, $location,
                contexts, contextService, historyModalService, $stateParams, contextResource, validationService) {
        'ngInject';

        $location.search('group', null);

        this.rootScope = $rootScope;
        _state.set(this, $state);
        _scope.set(this, $scope);
        _logger.set(this, logger);
        _dataSourceResource.set(this, dataSourceResource);
        _copyDataSourceModalService.set(this, copyDataSourceModalService);
        _approvalRequestModalService.set(this, approvalRequestModalService);
        _approvalCommentModalService.set(this, approvalCommentModalService);
        _historyModalService.set(this, historyModalService);
        _contextResource.set(this, contextResource);
        _validationService.set(this, validationService);

        this.approvalStatusConstants = approvalStatusConstants;
        this.username = session.name;

        this.tableOptions = {
            allowImports: false,
            addEmptyRow: false
        };
        contexts.$promise.then((resolvedContexts) => {
            this.resolvedContexts = resolvedContexts;
            this.context = contextService.getCurrentContextById(resolvedContexts,
                $stateParams.contextId ? $stateParams.contextId : 'systemId-1');
            _validationService.get(this).validateUser(this.context.id);
        });

        dataSourceIdentity.$promise.then((identity) => {
            this.dataSourceIdentity = identity;
            this.selectedGroup = identity.group;
            this.selectedDatasource = identity.id;
            this.selectedContextId = identity.contextId;
            this.selectedContextName = identity.context;
            this.currentContext = contextService.getCurrentContextById(this.resolvedContexts,
                                        identity.contextId);
            this.contextPath = contextService._getContextPath(this.resolvedContexts,
                                                                    this.currentContext).replace(/\s/g, '');
            this._populateApprovalData(identity);
            this.label = identity.label;
            this.version = identity.version;
        }, (failure) => {
            if ($state.params.version) {
                logger.errorWithToast('Failed to load datasource version ' + $state.params.version, failure.data.message);
                $state.go('base.contexts.datasources.view', {dataSourceId: $state.params.dataSourceId});
            } else {
                logger.errorWithToast('Failed to load datasource', failure.data.message);
                $state.go('base.contexts.datasources.list');
            }
        });

        dataSourceRecords.$promise.then((item) => {
            this.gridOptions = {
                enableColumnResizing: true,
                enableCellEditOnFocus: false,
                cellEditableCondition: false,
                enableGridMenu: true,
                exporterMenuPdf: false,
                exporterMenuExcel: false,
                data: item.data,
                columnDefs: [],
                columnOrder: item.meta.columnOrder
            };
        }, (failure) => {
            logger.errorWithToast('Failed to load datasource records.', failure.data.message);
        });

        dataSourceVersions.$promise.then((availableVersions) => {
            this.versions = [];
            for (let versionNumber of availableVersions) {
                this.versions.push({label: 'ver. ' + versionNumber, number: versionNumber});
            }
            dataSourceIdentity.$promise.then((identity) => {
                this.selectedVersion = this.versions.find((v) => {
                    return v.number === identity.version;
                });
            });
        });
        this.noAccessErrorMessage = 'You aren\'t a reviewer on this review, please contact the review requester';
        this.noOpenReviewErrorMessage = 'No review is currently open for this data source';
    }

    onVersionChange(selectedVersion) {
        _state.get(this).go('base.contexts.datasources.view-version', {
            dataSourceId: this.dataSourceIdentity.id,
            version: selectedVersion.number
        });
    }

    refreshPage() {
        _state.get(this).go('base.contexts.datasources.view', {
            dataSourceId: this.dataSourceIdentity.id
        }, {reload: true});
    }

    copyDataSource() {
        let modalInstance = _copyDataSourceModalService.get(this).open(
            this.dataSourceIdentity.id, this.dataSourceIdentity.name,
            this.resolvedContexts, this.selectedContextId,
            this.versions, this.selectedVersion,
            this.selectedGroup
        );

        modalInstance.result.then((dataSourceCopyRequest) => {
            this.rootScope.loading = true;
            _dataSourceResource.get(this).copy(dataSourceCopyRequest).$promise
                .then(
                    (result) => {
                        _logger.get(this).successWithToast('Data source successfully copied.', result);
                        _state.get(this).go('base.contexts.datasources.view', {dataSourceId: result.id}
                            , {reload: true});
                        this.rootScope.loading = false;
                    },
                    (failure) => {
                        _logger.get(this).errorWithToast('Failed to copy data source', failure.data.message);
                        this.rootScope.loading = false;
                    });
        });
    }

    copyContext(context) {
        let inputElement = document.createElement('input');
        inputElement.setAttribute('value', context.replace(/\s/g, ''));
        document.body.appendChild(inputElement);
        inputElement.select();
        document.execCommand('copy');
        document.body.removeChild(inputElement);
    }

    goToReviewPage() {
        _state.get(this).go('base.contexts.datasources.view.review',
            { dataSourceId: this.dataSourceIdentity.id, version: this.dataSourceIdentity.version });
    }

    showAllByVersion() {
        _historyModalService.get(this).open(this.dataSourceIdentity.id);
    }

    /* ---------------- Approval Status access control ---------------- */

    canRequestApproval() {
        return this.firstVersionSelected() && this.approvalStatus === this.approvalStatusConstants.UNAPPROVED;
    }

    canCancelRequest() {
        return this.isPendingRequest() && this._isUserSubmitter();
    }

    canApproveOrReject() {
        return this.isPendingRequest() && this._isUserReviewer();
    }

    canUnApprove() {
        if ((this.versions && this.selectedVersion) && !this.rootScope.isCustomerProfile) {
            for (let version of this.versions) {
                if (!version.number.endsWith('-SNAPSHOT')) {
                    return (version.number === this.selectedVersion.number && this.isApproved());
                }
            }
        }

        return false;
    }

    firstVersionSelected() {
        return this.selectedVersion && this.selectedVersion.number === _.first(this.versions).number;
    }

    isPendingRequest() {
        return this.approvalStatus === this.approvalStatusConstants.PENDING;
    }

    isApprovedOrRejected() {
        return this.isApproved() ||
            this.approvalStatus === this.approvalStatusConstants.REJECTED;
    }

    isApproved() {
        return this.approvalStatus === this.approvalStatusConstants.APPROVED;
    }

    isRejected() {
        return this.approvalStatus === this.approvalStatusConstants.REJECTED;
    }

    isUnApproved() {
        return this.approvalStatus === this.approvalStatusConstants.UNAPPROVED && this.approver;
    }

    _isUserSubmitter() {
        if ((this.dataSourceIdentity.reviewRequester === this.username.toLowerCase() ||
        this.dataSourceIdentity.reviewRequester === this.username.toUpperCase()) && this._isUserReviewer()) {
            return this._isUserReviewer();
        } else {
            return !this._isUserReviewer();
        }
    }

    _isUserReviewer() {
        return this.dataSourceIdentity.reviewers.includes(this.username.toUpperCase()) ||
        this.dataSourceIdentity.reviewers.includes(this.username.toLowerCase());
    }

    isNotSnapshot() {
        return this.version !== undefined && !this.version.endsWith('-SNAPSHOT');
    }


    /* ---------------- Approval Status actions ---------------- */

    requestApproval() {
        let modalInstance = _approvalRequestModalService.get(this).open(this.selectedContextId);

        modalInstance.result.then(result => {
            this._handleApproval(this.approvalStatusConstants.PENDING, result.selected, result.comment);
        });
    }

    cancelRequest() {
        this._handleApproval(this.approvalStatusConstants.CANCELLED);
    }

    rejectRequest() {
        this._approveOrReject(this.approvalStatusConstants.REJECTED);
    }

    approveRequest() {
        this._approveOrReject(this.approvalStatusConstants.APPROVED);
    }

    unApprove() {
        this._approveOrReject(this.approvalStatusConstants.UNAPPROVED);
    }

    saveLabel(value) {
        let labelObj = {
            dataSourceId: this.dataSourceIdentity.id,
            name: value,
            version: this.dataSourceIdentity.version,
            contextId: this.dataSourceIdentity.contextId
        };
        if (value) {
            return this._updateLabel(labelObj);
        } else if (this.label) {
            return this._deleteLabel(this.label, this.dataSourceIdentity.contextId);
        }
        _logger.get(this).errorWithToast('Cannot delete Label as none applied to Data Source');
    }

    _deleteLabel(label, contextId) {
        this.rootScope.loading = true;
        return _dataSourceResource.get(this).deleteLabel({label: label, contextId: contextId})
            .$promise.then((result) => {
                _logger.get(this).successWithToast('Label has been deleted', this.label);
                this.rootScope.loading = false;
                return true;
            }, (failure) => {
                _logger.get(this).errorWithToast('Failed to delete label', failure.data.message);
                this.rootScope.loading = false;
                return false;
            });
    }

    _updateLabel(labelObj) {
        this.rootScope.loading = true;
        return _dataSourceResource.get(this).addLabel(labelObj)
            .$promise.then((result) => {
                this.label = labelObj.name.trim();
                _logger.get(this).successWithToast('Label has been saved', this.label);
                this.rootScope.loading = false;
                return true;
            }, (failure) => {
                _logger.get(this).errorWithToast('Failed to save label', failure.data.message);
                this.rootScope.loading = false;
                return false;
            });
    }

    _approveOrReject(decision) {
        let modalInstance = _approvalCommentModalService.get(this).open(decision);
        modalInstance.result.then((comment) => {
            this._handleApproval(decision, [], comment, this.username);
        });
    }

    _handleApproval(newStatus, reviewers, comment, approver) {
        let hostname = window.location.host;

        let approvalRequest = {
            'status': newStatus,
            'reviewers': reviewers,
            'comment': comment,
            'approver': approver,
            'hostname': hostname
        };

        this.rootScope.loading = true;
        _dataSourceResource.get(this)
            .approve({id: this.dataSourceIdentity.id}, approvalRequest)
            .$promise
            .then(
                (result) => {
                    _logger.get(this)
                        .successWithToast('Approval status successfully updated to ' + result.approvalStatus, result);
                    this._populateApprovalData(result);
                    this.rootScope.loading = false;
                    this.refreshPage();
                },
                (failure) => {
                    _logger.get(this).errorWithToast('Failed to update data source approval status', failure.data.message);
                    this.rootScope.loading = false;
                });
    }

    _populateApprovalData(dataSource) {
        this.approvalStatus = dataSource.approvalStatus;
        this.reviewers = _.isEmpty(dataSource.reviewers) ? '-' : dataSource.reviewers.join(', ');
        this.reviewRequester = dataSource.reviewRequester;
        this.comment = dataSource.comment || '-';
        this.approver = dataSource.approver || '-';
    }

    getUserValidation() {
        return _validationService.get(this).getUserValidation();
    }
}
