const _state = new WeakMap();
const _logger = new WeakMap();
const _editActionsService = new WeakMap();
const _dataSourceResource = new WeakMap();
const _dataSourceService = new WeakMap();
const _historyModalService = new WeakMap();
const _stateChangeDialogService = new WeakMap();

export default class DataSourceEditController {
    constructor(dataSourceRecords, approvalStatusConstants,
                dataSourceResource,
                dataRecordsGridConstants, editActionsService,
                dataSourceService, logger,
                $state, $scope, $rootScope, groupList,
                contexts, contextService, $stateParams,
                dataSourceEditConstants, historyModalService,
                stateChangeDialogService) {
        'ngInject';

        this.rootScope = $rootScope;
        _state.set(this, $state);
        _logger.set(this, logger);
        _editActionsService.set(this, editActionsService);
        _dataSourceResource.set(this, dataSourceResource);
        _dataSourceService.set(this, dataSourceService);
        _historyModalService.set(this, historyModalService);
        _stateChangeDialogService.set(this, stateChangeDialogService);

        this.approvalStatusConstants = approvalStatusConstants;
        this.dataSourceEditConstants = dataSourceEditConstants;
        this.columnChanged = false;
        this.allowNavigation = false;

        this.groupAutoCompleteInputChange = this._setGroupName.bind(this);
        this.groupAutoCompleteEntrySelected = this._selectGroup.bind(this);

        $scope.$on(dataRecordsGridConstants.events.afterCellEdit, this._afterCellEditEvent.bind(this));
        $scope.$on(dataRecordsGridConstants.events.onDataRecordDelete, this._onDataRecordDeleteEvent.bind(this));
        $scope.$on(dataRecordsGridConstants.events.onColumnMove, this._onColumnMoveEvent.bind(this));

        $scope.$on(dataRecordsGridConstants.events.onColumnAdd, this._onColumnAddEvent.bind(this));
        $scope.$on(dataRecordsGridConstants.events.onColumnDelete, this._onColumnDeleteEvent.bind(this));
        $scope.$on(dataRecordsGridConstants.events.onColumnRename, this._onColumnRenameEvent.bind(this));
        $rootScope.$on('deleteEventValue', (event, data) => this._isImportedValue = data.value);

        contexts.$promise.then((resolvedContexts) => {
            this.context = contextService.getCurrentContextById(resolvedContexts,
                $stateParams.contextId ? $stateParams.contextId : 'systemId-1');
        });

        dataSourceResource.get({id: $stateParams.dataSourceId}).$promise.then((identity) => {
            this.dataSourceIdentity = identity;
            if (this.isPendingRequest()) {
                _logger.get(this).errorWithToast('Cannot edit a Data Source with a pending approval request');
                _state.get(this).go('base.contexts.datasources.view', {dataSourceId: identity.id});
            }

            this.oldDataSourceGroup = this.dataSourceIdentity.group;
            this.oldDataSourceName = this.dataSourceIdentity.name;
            this.dataSourceIdentity.version = _dataSourceService.get(this).incrementApprovedVersion(this.dataSourceIdentity.version);
            this.oldDataSourceVersion = this.dataSourceIdentity.version;
            this.oldDataSourceApprovalStatus = this.dataSourceIdentity.approvalStatus;

            this.selectedGroup = {
                groupName: this.dataSourceIdentity.group
            };

            this.errorInVersion = _dataSourceService.get(this).validateVersion(this.dataSourceIdentity.version);
        });

        dataSourceRecords.$promise.then((item) => {
            this.gridOptions = {
                enableColumnResizing: true,
                enableCellEditOnFocus: true,
                enableGridMenu: true,
                exporterMenuPdf: false,
                exporterMenuExcel: false,
                data: item.data,
                columnDefs: [],
                columnOrder: item.meta.columnOrder
            };
        });

        groupList.$promise.then((groups) => {
            this.availableGroups = groups.map(groupName => {
                return {groupName: groupName};
            });
        });
        this.tableOptions = {
            allowImports: true,
            addEmptyRow: false
        };
        this.dataRecordChangeActions = [];

        $scope.$on('$stateChangeStart', (event) => {
            this._addRestrictionToState(event);
        });
    }

    editVersionChange(version) {
        this.dataSourceIdentity.version = version;
        this.errorInVersion = _dataSourceService.get(this).validateVersion(this.dataSourceIdentity.version);
    }

    isPendingRequest() {
        return this.dataSourceIdentity && this.dataSourceIdentity.approvalStatus === this.approvalStatusConstants.PENDING;
    }

    isDisabled() {
        return this.isPendingRequest() || this.rootScope.loading;
    }

    showAllByVersion() {
        _historyModalService.get(this).open(this.dataSourceIdentity.id);
    }

    save() {
        this.allowNavigation = true;
        this.rootScope.loading = true;
        this.dataSourceIdentity.name = _dataSourceService.get(this).trimName(this.dataSourceIdentity.name);
        this.errorInName = _dataSourceService.get(this).validateName(this.dataSourceIdentity.name);
        this.errorInGroup = _dataSourceService.get(this).validateGroup(this.dataSourceIdentity.group);
        this.errorInVersion = _dataSourceService.get(this).validateVersion(this.dataSourceIdentity.version);
        this.errorInDataRecords = _dataSourceService.get(this).validateDataRecords(this.gridOptions.data);

        if (this.columnChanged) {
            this._createColumnPositions();
        }

        if (this.errorInName || this.errorInGroup || this.errorInDataRecords || this.errorInVersion) {
            _logger.get(this).errorWithToast('Error in data source fields. Please verify model.');
            this.rootScope.loading = false;
        } else {
            let identityActions = this._createIdentityChangeActions();
            if (this._isImportedValue) {
                for (let eachAction of this.dataRecordChangeActions) {
                    if (eachAction.type === 'RECORD_VALUE_EDIT') {
                        this.dataRecordChangeActions.splice(this.dataRecordChangeActions.indexOf(eachAction));
                    }
                }
                this._assembleGridDataAsActions();
            }

            if (!this.columnChanged) {
                for (let action of this.dataRecordChangeActions) {
                    if (action.type === 'RECORD_KEY_RENAME' || action.type === 'RECORD_KEY_ADD' || action.type === 'RECORD_KEY_DELETE') {
                        this._updateColumnOrderActions();
                        break;
                    }
                }
            }

            let actionsToSend = identityActions.concat(this.dataRecordChangeActions);

            if (actionsToSend.length === 0) {
                _logger.get(this).errorWithToast('No changes in datasource detected, failed to save');
                this.rootScope.loading = false;
            } else {
                _dataSourceResource.get(this).update({id: this.dataSourceIdentity.id}, actionsToSend).$promise.then(
                    (result) => {
                        this.rootScope.loading = false;
                        _logger.get(this).successWithToast('Data source successfully updated.', result);
                        _state.get(this).go('base.contexts.datasources.view', {dataSourceId: result.id}, {reload: true});
                    },
                    (failure) => {
                        _logger.get(this).errorWithToast('Failed to update data source', failure.data.message);
                        this.rootScope.loading = false;
                    });
            }
        }
    }

    _assembleGridDataAsActions() {

        let isFirst = true;
        for (let dataRecordRow of this.gridOptions.data) {
            if (!dataRecordRow.emptyRow) {
                if (isFirst) {
                    let keys = Object.keys(dataRecordRow.values).filter((v) => !v.startsWith('$'));

                    for (let column of keys) {
                        let action1 = _editActionsService.get(this).addColumnAction(this.dataSourceIdentity.version, column);
                        this.dataRecordChangeActions.push(action1);
                    }
                    isFirst = false;
                }
            }
        }
        for (let dataRecordRow of this.gridOptions.data) {
            if (!dataRecordRow.emptyRow) {
                let keys = Object.keys(dataRecordRow.values).filter((v) => !v.startsWith('$'));
                for (let key of keys) {
                    if (keys.indexOf(key) === 0) {
                        let action = _editActionsService.get(this).addRecordAction(this.dataSourceIdentity.version, dataRecordRow, key,
                        dataRecordRow.values[key], null);
                        this.dataRecordChangeActions.push(action);
                    } else {
                        let action = _editActionsService.get(this).editRecordAction(this.dataSourceIdentity.version, dataRecordRow, key,
                        dataRecordRow.values[key], null);
                        this.dataRecordChangeActions.push(action);
                    }
                }
            }
        }
    }

    _createIdentityChangeActions() {
        let actions = [];
        if (this.oldDataSourceGroup !== this.dataSourceIdentity.group) {
            let action = _editActionsService.get(this).identityGroupChangeAction(this.dataSourceIdentity.version,
                this.dataSourceIdentity.group);
            actions.push(action);
        }
        if (this.oldDataSourceName !== this.dataSourceIdentity.name) {
            let action = _editActionsService.get(this).identityNameChangeAction(this.dataSourceIdentity.version,
                this.dataSourceIdentity.name);
            actions.push(action);
        }
        if (this.oldDataSourceVersion !== this.dataSourceIdentity.version) {
            let action = _editActionsService.get(this).identityVersionChangeAction(this.dataSourceIdentity.version,
                this.dataSourceIdentity.version);
            actions.push(action);
        }
        if (this.oldDataSourceApprovalStatus !== this.dataSourceEditConstants.unapproved) {
            let action = _editActionsService.get(this).identityApprovalChangeAction(this.dataSourceIdentity.version,
                this.dataSourceEditConstants.unapproved);
            actions.push(action);
        }
        return actions;
    }

    _setGroupName(value) {
        this.dataSourceIdentity.group = value;
    }

    _selectGroup(selected) {
        if (selected) {
            this.dataSourceIdentity.group = selected.originalObject.groupName;
        }
    }

    _afterCellEditEvent(event, rowEntity, colDef, newValue, oldValue, isNewRow) {
        if (isNewRow) {
            // add record
            let action = _editActionsService.get(this).addRecordAction(this.dataSourceIdentity.version,
                rowEntity, colDef.propertyKey, newValue);
            this.dataRecordChangeActions.push(action);
        } else if (rowEntity.id) {
            // edit record
            let action = _editActionsService.get(this).editRecordAction(this.dataSourceIdentity.version,
                rowEntity, colDef.propertyKey, newValue, oldValue);
            this.dataRecordChangeActions.push(action);
        }
    }

    _onColumnAddEvent(event, newPropertyKey) {
        let action = _editActionsService.get(this).addColumnAction(this.dataSourceIdentity.version, newPropertyKey);
        this.dataRecordChangeActions.push(action);
    }

    _onColumnDeleteEvent(event, propertyKey) {
        let action = _editActionsService.get(this).deleteColumnAction(this.dataSourceIdentity.version, propertyKey);
        this.dataRecordChangeActions.push(action);
    }

    _onColumnRenameEvent(event, newPropertyKey, oldPropertyKey) {
        let action = _editActionsService.get(this).renameColumnAction(this.dataSourceIdentity.version, newPropertyKey, oldPropertyKey);
        this.dataRecordChangeActions.push(action);
    }

    _onDataRecordDeleteEvent(event, recordToDelete) {
        let action = _editActionsService.get(this).deleteRecordAction(this.dataSourceIdentity.version, recordToDelete);
        this.dataRecordChangeActions.push(action);
    }

    _onColumnMoveEvent(event, columns) {
        let newColumnOrder = [];
        columns.forEach((item) => {
            if (item.displayName.length > 0) {
                newColumnOrder.push(item.displayName);
            }
        });

        this.columnChanged = newColumnOrder;
    }

    _createColumnPositions() {
        let index = 0;
        this.columnChanged.forEach((item) => {
            let action = _editActionsService.get(this).changeColumnOrder(this.dataSourceIdentity.version,
            item, index);
            this.dataRecordChangeActions.push(action);
            index++;
        });
        this.columnChanged = false;
    }
    _updateColumnOrderActions() {
        let index = 0;
        for (let column of this.gridOptions.columnDefs) {
            let action = _editActionsService.get(this).changeColumnOrder(this.dataSourceIdentity.version,
              column.propertyKey, index);
            this.dataRecordChangeActions.push(action);
            index++;
        }
    }
    _addRestrictionToState(event) {
        if (_state.get(this).current.name === 'base.contexts.datasources.view.edit' && !this.allowNavigation) {
            event.preventDefault();
            let modalInstance = _stateChangeDialogService.get(this).open();
            modalInstance.result.then((answer) => {
                if (answer) {
                    this.allowNavigation = true;
                    _state.get(this).go('base.contexts.datasources.view', {reload: true});
                }
            });
        }
        this.allowNavigation = false;
    }
}
