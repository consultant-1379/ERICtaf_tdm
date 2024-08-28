const _scope = new WeakMap();
const _renameColumnModalService = new WeakMap();
const _addColumnModalService = new WeakMap();
const _dataRecordsGridConstants = new WeakMap();
const _uiGridConstants = new WeakMap();
const _dataSourceService = new WeakMap();
const _modal = new WeakMap();
const _rootScope = new WeakMap();

export default class DataRecordsGridDirectiveController {
    constructor(renameColumnModalService, addColumnModalService, dataRecordsGridConstants, dataSourceService, uiGridConstants, $scope,
    $uibModal, $state, $rootScope) {
        'ngInject';
        this.state = $state;
        _rootScope.set(this, $rootScope);
        _scope.set(this, $scope);
        _renameColumnModalService.set(this, renameColumnModalService);
        _addColumnModalService.set(this, addColumnModalService);
        _dataRecordsGridConstants.set(this, dataRecordsGridConstants);
        _uiGridConstants.set(this, uiGridConstants);
        _dataSourceService.set(this, dataSourceService);
        _modal.set(this, $uibModal);

        $scope.$on('deleteColumnEvent', (event, data) => {
            this.col = {
                field: data.columnField
            };
            this._deleteColumn();
        });

        $scope.$on('renameColumnEvent', (event, data) => {
            this.col = {
                field: data.columnField,
                propertyKey: data.columnField
            };
            this._renameColumn();
        });

        $scope.$watch('vm.gridOptions.columnDefs', () => {
            if (this.importedData && !this.readOnly) {
                this._addColumnDropdownOptions();
            }
        });

        this.newColumnName = null;
        this.displayImportButton = this.table.allowImports;
        window.onerror = this._unhandledImportErrorCallback.bind(this);

        this.options.importerDataAddCallback = this._importCsvData.bind(this);
        this.options.importerErrorCallback = this._errorHandlerCallback.bind(this);
        this.initGridProperties();
    }

    initGridProperties() {
        if (this.readOnly) {
            this._cutEmptyRow();
            if (this.options.data.length === 0) {
                this.error = {message: 'No Data Records available.'};
            }

            this.options.enableRowSelection = false;
            this.options.enableRowHeaderSelection = false;
        }

        this.options.enableFiltering = false;
        this.options.importerShowMenu = false;

        if (this.options.data.length > 0) {
            this._extractColumnFields();
        }

        this.options.onRegisterApi = this._onGridReady.bind(this);

        // don't init this values before grid options properly constructed
        this.data = this.options.data;

        if (this.table.addEmptyRow) {
            this._addDefaultEmptyDataRecord();
        }

        this.columns = this.options.columnDefs;
        this.gridOptions = this.options;
    }

    showCsvImportDialog() {
        let importer = this.gridApi.importer;

        this.importFileModal = _modal.get(this).open({
            animation: true,
            templateUrl: 'app/datasources/import/csv-import-modal.html',
            controller: 'csvImportModalController',
            controllerAs: 'vm',
            size: 'sm'
        });

        this.importFileModal.result.then((file) => {
            importer.importFile(file);
            this._clearTableData();
        });
        this.deletePreviousRowsBeforeImport();
    }

    _addColumnDropdownOptions() {
        for (let columnDef of this.options.columnDefs) {
            if (!columnDef.menuItems) {
                columnDef.menuItems = [];
            }
            columnDef.menuItems = _dataSourceService.get(this).addDropdownOptionsToColumns();
            columnDef.field = columnDef.name;
        }
        this.columns = this.options.columnDefs;
        this.gridApi.core.refresh();
        this.gridApi.core.notifyDataChange(_uiGridConstants.get(this).dataChange.COLUMN);
    }

    _updateColumnDefs(columns) {
        let columnOrder = this.options.columnOrder;
        let unstoredColumns = [];
        if (columnOrder) {
            for (let column of columns) {
                let position = columnOrder[column.propertyKey];
                if (position) {
                    if (!this.options.columnDefs[position]) {
                        this.options.columnDefs[position] = column;
                    } else {
                        this.options.columnDefs[++position] = column;
                    }
                } else {
                    unstoredColumns.push(column);
                }
            }
            for (let col of unstoredColumns) {
                this.options.columnDefs.push(col);
            }
        } else {
            for (let column of columns) {
                this.options.columnDefs.push(column);
            }
        }
    }

    _errorHandlerCallback(grid, errorKey, message, context) {
        this._displayError();
    }

    _unhandledImportErrorCallback(errorMsg, url, lineNumber) {
        this._displayError();
    }

    _displayError() {
        this.error = {
            message: 'An error has occurred importing this csv file. ' +
            'Please check that the csv structure is valid.'
        };
        this.gridApi.core.refresh();
    }
    _importCsvData(grid, csvData) {
        this.error = {};
        this.importedData = true;
        this.gridOptions.data = csvData;
        let formattedCsvData = _dataSourceService.get(this)._mapImportedDataForSaving(this.gridOptions.data);
        this.gridOptions.data = [];

        for (let data of formattedCsvData) {
            this.gridOptions.data.push(data);
        }

        if (this.options.data.length > 0) {
            this._extractColumnFields();
        }

        this.data = this.gridOptions.data;
        this.columns = this.options.columnDefs;
        this.gridApi.core.refresh();
    }


  deletePreviousRowsBeforeImport() {
      this.gridApi.selection.selectAllRows(this.gridApi.grid);
      let selectedRows = this.gridApi.selection.getSelectedRows();
      _rootScope.get(this).$emit('deleteEventValue', {value: 'true'});
      for (let selectedRow of selectedRows) {
          let eventName = _dataRecordsGridConstants.get(this).events.onDataRecordDelete;
          _scope.get(this).$emit(eventName, selectedRow);
      }
      this.gridApi.selection.clearSelectedRows();
  }
    _extractColumnFields() {
        this.tablePopulated = true;
        let columns = _dataSourceService.get(this).extractColumnFields(this.options.data, this.readOnly,
           this._highlightModifiedCells());
        this._updateColumnDefs(columns);
    }

    _highlightModifiedCells() {
        return this.approvalStatus === 'PENDING' && this.state.current.name.endsWith('review');
    }

    _cutEmptyRow() {
        let emptyRowIndex = this.options.data.findIndex((r) => {
            return r.emptyRow;
        });
        if (emptyRowIndex > -1) {
            this.options.data.splice(emptyRowIndex, 1);
        }
    }

    _onGridReady(gridApi) {
        this.gridApi = gridApi;
        this.gridApi.edit.on.afterCellEdit(null, this._afterCellEdit.bind(this));
        this.gridApi.colMovable.on.columnPositionChanged(null, this.onColumnPositionChange.bind(this));

        if (!this.readOnly) {
            if (this.columns.length === 0) {
                this.columns.push(_dataSourceService.get(this)
                    .createDefaultColumnObject(_dataRecordsGridConstants.get(this).propertyKeyPrefix + 'col1'));
            }
        }
    }

    onColumnPositionChange(colDef, originalPosition, newPosition) {
        let eventName = _dataRecordsGridConstants.get(this).events.onColumnMove;
        _scope.get(this).$emit(eventName, this.gridApi.grid.columns);
    }

    toggleFiltering() {
        this.gridOptions.enableFiltering = !this.gridOptions.enableFiltering;
        this.gridApi.core.notifyDataChange(_uiGridConstants.get(this).dataChange.COLUMN);
    }

    hasRowsSelected() {
        if (this.gridApi) {
            return this.gridApi.selection.getSelectedCount() > 0;
        }
        return false;
    }
    openAddColumnPopUp() {
        let valueColumnKeys = this._getValueColumnPropertyKeys();
        let modalInstance = _addColumnModalService.get(this).open(valueColumnKeys);
        modalInstance.result.then((column) => {
            this.newColumnName = column;
            this.newColumn = _dataSourceService.get(this)
             .createDefaultColumnObject(_dataRecordsGridConstants.get(this).propertyKeyPrefix + this.newColumnName, this.readOnly);
            this.columns.push(this.newColumn);

             // emit onColumnAdd
            let eventName = _dataRecordsGridConstants.get(this).events.onColumnAdd;
            _scope.get(this).$emit(eventName, this.newColumn.propertyKey);

            if (this.columns.length === 1) {
                this._addDefaultEmptyDataRecord();
            }

            this.gridApi.core.refresh();
        });
    }
    hasData() {
        let rows = this.gridApi.grid.rows;
        if (rows.length !== 0) {
            for (let row in rows) {
                if (rows[row].entity.emptyRow === false) {
                    return true;
                }
            }
        }
        return false;
    }

    deleteSelectedRows() {
        let selectedRows = this.gridApi.selection.getSelectedRows();
        for (let selectedRow of selectedRows) {
            this.data.splice(this.data.indexOf(selectedRow), 1);
            // emit onDataRecordDelete
            let eventName = _dataRecordsGridConstants.get(this).events.onDataRecordDelete;
            _scope.get(this).$emit(eventName, selectedRow);
        }

        this.gridApi.selection.clearSelectedRows();
    }

    addRow() {
        let timestamp = Date.now();
        this.data.push(_dataSourceService.get(this).createDefaultRowObject(timestamp));
    }

    _afterCellEdit(rowEntity, colDef, newValue, oldValue) {
        if (newValue === oldValue) {
            return;
        }
        if (newValue !== oldValue) {
            colDef.cellClass = function(grid, row, col) {
                row.entity.dirty = row.entity.dirty || {};
                if (rowEntity.id === row.entity.id) {
                    rowEntity.dirty[col.displayName] = true;
                }
                if (row.entity.dirty[col.displayName]) {
                    return 'cell-edit';
                }
                return '';
            };
            this.gridApi.core.notifyDataChange(_uiGridConstants.get(this).dataChange.COLUMN);
            // emit afterCellEdit
            let eventName = _dataRecordsGridConstants.get(this).events.afterCellEdit;
            let isNewRow = rowEntity.emptyRow ? rowEntity.emptyRow : false;
            _scope.get(this).$emit(eventName, rowEntity, colDef, newValue, oldValue, isNewRow);
        }
        if (rowEntity.emptyRow && newValue && this.importedData) {
            let timestamp = Date.now();
            this.data.push(_dataSourceService.get(this).createDefaultRowObject(timestamp));
            let newDataRow = this.data[this.data.length - 1];
            newDataRow.values = {};
            newDataRow.values[colDef.propertyKey] = newValue;
            delete newDataRow.emptyRow;
        }

        if (rowEntity.emptyRow) {
            this.importedData = false;
        }

        if (!this.importedData) {
            for (let data of this.data) {
                if (data.id === rowEntity.id) {
                    data.emptyRow = false;
                }
            }
        }

        this.options.data = this.data;
        this.gridApi.core.refresh();
    }

    _addDefaultEmptyDataRecord() {
        let timestamp = Date.now();
        this.data.push(_dataSourceService.get(this).createDefaultRowObject(timestamp));
    }

    _renameColumn() {
        let columnToRename = this._getCurrentlySelectedColumn();
        let valueColumnKeys = this._getValueColumnPropertyKeys();
        let modalInstance = _renameColumnModalService.get(this).open(columnToRename.propertyKey, valueColumnKeys);
        let valueFieldName = _dataRecordsGridConstants.get(this).valueFieldName;

        modalInstance.result.then((newPropertyKey) => {
            if (newPropertyKey !== columnToRename.propertyKey) {
                for (let dataRecord of this.data) {
                    if (dataRecord[valueFieldName]) {
                        dataRecord[valueFieldName][newPropertyKey] = dataRecord[valueFieldName][columnToRename.propertyKey];
                        delete dataRecord[valueFieldName][columnToRename.propertyKey];
                    }
                }

                let newColumn = _dataSourceService.get(this).createDefaultColumnObject(
                    _dataRecordsGridConstants.get(this).propertyKeyPrefix + newPropertyKey,
                    columnToRename.width);

                let oldColumnIndex = this.columns.indexOf(columnToRename);
                this.columns[oldColumnIndex] = newColumn;

                // emit onColumnRename
                let eventName = _dataRecordsGridConstants.get(this).events.onColumnRename;
                _scope.get(this).$emit(eventName, newPropertyKey, columnToRename.propertyKey);
            }
        });
    }

    _getValueColumnPropertyKeys() {
        return this.columns
            .filter((col) => {
                return col.field.startsWith(_dataRecordsGridConstants.get(this).propertyKeyPrefix);
            })
            .map((col) => {
                return col.propertyKey;
            });
    }

    _deleteColumn() {
        let columnToDelete = this._getCurrentlySelectedColumn();
        let columnIndex = this.columns.indexOf(columnToDelete);
        this.columns.splice(columnIndex, 1);

        let valueFieldName = _dataRecordsGridConstants.get(this).valueFieldName;
        for (let dataRecord of this.data) {
            if (dataRecord[valueFieldName]) {
                delete dataRecord[valueFieldName][columnToDelete.propertyKey];
            }
        }

        // emit onColumnDelete
        let eventName = _dataRecordsGridConstants.get(this).events.onColumnDelete;
        _scope.get(this).$emit(eventName, columnToDelete.propertyKey);
    }

    _getCurrentlySelectedColumn() {
        return this.columns.filter((col) => {
            return col.field === this.col.field;
        })[0];
    }

    _clearTableData() {
        this.gridOptions.columnDefs = [];
        this.gridOptions.data = [];
    }
}
