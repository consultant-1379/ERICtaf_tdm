const _state = new WeakMap();
const _logger = new WeakMap();
const _dataSourceResource = new WeakMap();
const _dataSourceService = new WeakMap();
const _stateChangeDialogService = new WeakMap();
const defaultDsName = 'Data source name';

export default class DataSourceCreateController {
    constructor(dataSourceResource, dataSourceService, logger, $state, $scope, $rootScope, groupList,
                $stateParams, contexts, contextService, dataSourceEditConstants, stateChangeDialogService) {
        'ngInject';

        this.rootScope = $rootScope;
        _state.set(this, $state);
        _logger.set(this, logger);
        _dataSourceResource.set(this, dataSourceResource);
        _dataSourceService.set(this, dataSourceService);
        _stateChangeDialogService.set(this, stateChangeDialogService);

        this.groupAutoCompleteInputChange = this._setGroupName.bind(this);
        this.groupAutoCompleteEntrySelected = this._selectGroup.bind(this);
        this.dataSourceEditConstants = dataSourceEditConstants;

        contexts.$promise.then((resolvedContexts) => {
            this.context = contextService.getCurrentContextById(resolvedContexts,
                $stateParams.contextId ? $stateParams.contextId : 'systemId-1');
        });

        /* Validation flags */
        this.errorInCustomProperties = false;
        this.errorInGroup = false;
        this.tableOptions = {
            allowImports: true,
            addEmptyRow: true
        };

        this.dataSourceIdentity = {
            name: defaultDsName,
            group: null
        };

        groupList.$promise.then((groups) => {
            this.availableGroups = groups.map(groupName => {
                return {groupName: groupName};
            });
        });

        this.gridOptions = {
            enableColumnResizing: true,
            enableCellEditOnFocus: true,
            enableGridMenu: true,
            exporterMenuPdf: false,
            data: [],
            columnDefs: []
        };

        this.allowNavigation = false;
        $scope.$on('$stateChangeStart', (event) => {
            this._addRestrictionToState(event);
        });
    }

    editVersionChange(version) {
        this.dataSourceIdentity.version = version;
        this.errorInVersion = _dataSourceService.get(this).validateVersionWithBlanks(this.dataSourceIdentity.version);
    }

    createDataSource() {
        this.allowNavigation = true;
        this.dataSourceIdentity.name = _dataSourceService.get(this).trimName(this.dataSourceIdentity.name);
        this.errorInName = _dataSourceService.get(this).validateName(this.dataSourceIdentity.name, defaultDsName);
        this.errorInGroup = _dataSourceService.get(this).validateGroup(this.dataSourceIdentity.group);
        this.errorInDataRecords = _dataSourceService.get(this).validateDataRecords(this.gridOptions.data);
        this.errorInVersion = _dataSourceService.get(this).validateVersionWithBlanks(this.dataSourceIdentity.version);

        if (this.errorInName) {
            this._logErrorWithToast('Data source name is empty or incorrect');
        } else if (this.errorInGroup) {
            this._logErrorWithToast('Group is empty or incorrect');
        } else if (this.errorInVersion) {
            this._logErrorWithToast('Version is incorrect');
        } else if (this.errorInDataRecords) {
            this._logErrorWithToast(this.errorInDataRecords.message);
        } else {
            this.rootScope.loading = true;
            let dataSource = this._assembleDataSourceObject();
            _dataSourceResource.get(this).create(dataSource).$promise.then(
                (result) => {
                    _logger.get(this).successWithToast('Data source successfully created.', result);
                    _state.get(this).go('base.contexts.datasources.view', {dataSourceId: result.id}, {reload: true});
                    this.rootScope.loading = false;
                },
                (failure) => {
                    this._logErrorWithToastAndData('Failed to create data source', failure.data.message);
                    this.rootScope.loading = false;
                });
        }
    }

    _logErrorWithToast(message) {
        _logger.get(this).errorWithToast(message);
    }

    _logErrorWithToastAndData(message, data) {
        _logger.get(this).errorWithToast(message, data);
    }

    _assembleDataSourceObject() {
        let dataSourceObject = {
            identity: {
                name: null,
                group: null
            },
            records: []
        };

        dataSourceObject.identity.name = this.dataSourceIdentity.name;
        dataSourceObject.identity.group = this.dataSourceIdentity.group;

        dataSourceObject.identity.contextId = this.context.id;

        if (this.dataSourceIdentity.version) {
            dataSourceObject.identity.version = this.dataSourceIdentity.version;
        }

        for (let dataRecordRow of this.gridOptions.data) {
            if (!dataRecordRow.emptyRow) {
                let dataRecord = {
                    values: {}
                };
                let keys = Object.keys(dataRecordRow.values).filter((v) => !v.startsWith('$'));
                for (let key of keys) {
                    dataRecord.values[key] = dataRecordRow.values[key];
                }
                dataSourceObject.records.push(dataRecord);
            }
        }
        return dataSourceObject;
    }

    _setGroupName(value) {
        this.dataSourceIdentity.group = value;
    }

    _selectGroup(selected) {
        if (selected) {
            this.dataSourceIdentity.group = selected.originalObject.groupName;
        }
    }

    _addRestrictionToState(event) {
        if (_state.get(this).current.name === 'base.contexts.datasources.create' && !this.allowNavigation) {
            event.preventDefault();
            let modalInstance = _stateChangeDialogService.get(this).open();
            modalInstance.result.then((answer) => {
                if (answer) {
                    this.allowNavigation = true;
                    _state.get(this).go('base.contexts.datasources.list');
                }
            });
        }
        this.allowNavigation = false;
    }
}
