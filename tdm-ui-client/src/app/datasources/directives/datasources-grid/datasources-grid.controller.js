const _state = new WeakMap();
const _scope = new WeakMap();
const _dataSourceResource = new WeakMap();
const _dataSourcesGridConstants = new WeakMap();
const _logger = new WeakMap();
const _contextResource = new WeakMap();
const _validationService = new WeakMap();

export default class DataSourcesGridDirectiveController {
    constructor($state, $scope, $rootScope, dataSourceResource, dataSourcesGridConstants, logger,
                approvalStatusConstants, contextResource, validationService) {
        'ngInject';

        _state.set(this, $state);
        _scope.set(this, $scope);
        _logger.set(this, logger);
        _dataSourceResource.set(this, dataSourceResource);
        _dataSourcesGridConstants.set(this, dataSourcesGridConstants);
        _contextResource.set(this, contextResource);
        _validationService.set(this, validationService);

        this.approvalStatusConstants = approvalStatusConstants;

        this.initGridProperties();
        this.rootScope = $rootScope;

        _scope.get(this).$on(dataSourcesGridConstants.events.onClearGridRows, this.clearRows.bind(this));
    }

    initGridProperties() {
        let columnDefs = [
            {name: 'Name', field: 'name'},
            {name: 'Group', field: 'group'},
            {name: 'Context', field: 'context'},
            {version: 'Version', field: 'version'},
            {
                version: 'Approval Status', field: 'approvalStatus', cellClass: 'approvalStatus',
                cellTemplate: 'app/datasources/directives/datasources-grid/approval-status.html'
            },
            {name: 'CreatedBy', field: 'createdBy'},
            {name: 'CreationTime', field: 'createTime', cellFilter: 'date: "yyyy-MM-dd HH:mm:ss.sss"'}
        ];

        this.gridOptions = {
            enableSorting: true,
            enableColumnResizing: true,
            enableCellEditOnFocus: false,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            enableGridMenu: true,
            multiSelect: false,
            data: this.identities,
            onRegisterApi: this._onGridReady.bind(this),
            columnDefs: columnDefs
        };
    }

    hasRowsSelected() {
        if (this.gridApi) {
            return this.gridApi.selection.getSelectedCount() > 0;
        }
        return false;
    }


    viewSelected() {
        _state.get(this).go('base.contexts.datasources.view', {dataSourceId: this.selectedRow().id});
    }

    editSelected() {
        _state.get(this).go('base.contexts.datasources.view.edit', {dataSourceId: this.selectedRow().id});
    }

    deleteSelected() {
        this.rootScope.loading = true;
        let selectedRow = this.selectedRow();
        _dataSourceResource.get(this).delete({id: selectedRow.id}).$promise.then(() => {
            let eventName = _dataSourcesGridConstants.get(this).events.onDataSourceDelete;
            _scope.get(this).$emit(eventName, selectedRow);
            this.gridApi.selection.clearSelectedRows();
            _logger.get(this).successWithToast('Data source successfully deleted.');
            this.rootScope.loading = false;
        }, (failure) => {
            _logger.get(this).errorWithToast('Failed to delete data source', failure.data.message);
            this.rootScope.loading = false;
        });
    }

    hasAccess() {
        let selectedRow = this.selectedRow();
        this.currentContextId = selectedRow.contextId;

        if (this.oldContextId !== this.currentContextId) {
            this.oldContextId = this.currentContextId;
            _validationService.get(this).validateUser(this.currentContextId);
        }
        return _validationService.get(this).getUserValidation();
    }

    selectedRow() {
        return this.gridApi.selection.getSelectedRows()[0];
    }

    clearRows() {
        this.gridApi.selection.clearSelectedRows();
    }

    _onGridReady(gridApi) {
        this.gridApi = gridApi;
    }

    isPendingRequest() {
        if (this.selectedRow()) {
            return this.selectedRow().approvalStatus === this.approvalStatusConstants.PENDING;
        } else {
            return false;
        }
    }
}
