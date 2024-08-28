const _dataRecordsGridConstants = new WeakMap();

export default class DataSourceService {
    constructor(dataRecordsGridConstants, $rootScope, dataSourceEditConstants) {
        'ngInject';
        _dataRecordsGridConstants.set(this, dataRecordsGridConstants);
        this.$rootScope = $rootScope;
        this.approvedVersionExp = '^[0-9]+[.][0-9]+[.][0-9]';
        this.versionExp = this.approvedVersionExp + '+-SNAPSHOT$';

        this.dataSourceEditConstants = dataSourceEditConstants;

    }

    validateGroup(group) {
        return !group;
    }

    validateVersion(version) {
        let validVersion = new RegExp(this.versionExp);
        return !validVersion.test(version);
    }

    validateApprovedVersion(version) {
        let validVersion = new RegExp(this.approvedVersionExp);
        return !validVersion.test(version);
    }

    validateVersionWithBlanks(version) {
        if (!version) {
            return false;
        }
        let validVersion = new RegExp(this.versionExp);
        return !validVersion.test(version);
    }

    validateDataRecords(dataRecords) {
        if (dataRecords.length === 0) {
            return { message: 'The datasource is empty'};
        }
        for (let dataRecord of dataRecords) {
            if (dataRecord.emptyRow) {
                return {message: 'The datasource has at least one empty row'};
            }
            let allCellsEmpty = true;
            for (let key of Object.keys(dataRecord.values)) {
                if (dataRecord.values[key] === null) {
                    allCellsEmpty = allCellsEmpty && dataRecord.values[key] === null;
                } else {
                    allCellsEmpty = allCellsEmpty && dataRecord.values[key].length === 0;
                }
            }
            if (allCellsEmpty) {
                return {message: 'The datasource has at least one empty row'};
            }
        }
        return null;
    }

    validateName(name, defaultDsName) {
        return !name || name === defaultDsName;
    }

    incrementApprovedVersion(version) {
        if ((this.validateVersionWithBlanks(version) && this.validateApprovedVersion(version)) || version === null) {
            return version;
        } else if (version.endsWith('-SNAPSHOT')) {
            return version;
        } else {
            let parts = version.split('.');
            let incrementedBuildNumber = parseInt(parts[2]) + 1;
            let newVersion = parts[0] + '.' + parts[1] + '.' + incrementedBuildNumber.toString() + '-SNAPSHOT';
            return newVersion;
        }
    }

    trimName(name) {
        return (name.replace(this.dataSourceEditConstants.htmlTagRegex, '').trim());
    }

    /**
     * Extract data record property keys and construct columns with proper name and field mapping.
     * Aligned with Data Record object, which has values map and other meta properties.
     * @param dataArray -  array of data
     * @returns {Array}
     * @private
     */
    extractColumnFields(dataArray, readOnly, highlightModifiedColumns) {
        this.readOnly = readOnly;
        this.highlightModifiedColumns = highlightModifiedColumns;

        let columns = [];
        dataArray.forEach((dataObject) => {
            let keys = this.extractFieldKeys(dataObject, readOnly);
            this.addFieldColumns(keys, columns);
        });
        return this.uniqueColumns(columns);
    }

    extractFieldKeys(dataObject) {
        let keys =
            Object.keys(dataObject.values)
                .map((valueKey) => {
                    return _dataRecordsGridConstants.get(this).propertyKeyPrefix + valueKey;
                })
                .concat(Object.keys(dataObject))
                .filter((key) => {
                    return !key.startsWith('$$') && _dataRecordsGridConstants.get(
                            this).excludedFromRenderColumns.indexOf(key) < 0;
                });

        return keys;
    }

    addFieldColumns(keys, columns) {

        for (let key of keys) {
            let column = this.createDefaultColumnObject(key, this.readOnly);
            columns.push(column);
        }
    }

    createDefaultRowObject(timestamp) {
        function guid() {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }

            function s5() {
                return timestamp.toString();
            }
            return 'local_' + s5() + '-' + s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
        }

        return {emptyRow: true, id: guid()};
    }

    createDefaultColumnObject(fullPropertyKey, readOnly) {
        let highlightModifiedColumns = this.highlightModifiedColumns;
        let propertyKey = fullPropertyKey.replace(_dataRecordsGridConstants.get(this).propertyKeyPrefix, '');
        let column = {
            propertyKey: propertyKey,
            name: propertyKey,
            displayName: propertyKey,
            field: fullPropertyKey,
            enableCellEdit: !readOnly,
            headerTooltip: true,
            minWidth: 100,
            type: 'string',
            cellTooltip: function(row, col) {
                if (readOnly && highlightModifiedColumns) {
                    for (let cell of row.entity.modifiedColumns) {
                        if (cell === col.displayName) {
                            if (row.entity.oldValues == null) {
                                return 'No previous value';
                            }
                            let oldValue = row.entity.oldValues[cell];
                            return oldValue === '' || oldValue == null ? 'No previous value' : oldValue;
                        }
                    }
                }
            },
            cellClass: function(grid, row, col) {
                if (readOnly && highlightModifiedColumns) {
                    if (row.entity.deleted) {
                        return 'cell-delete';
                    }
                    for (let cell of row.entity.modifiedColumns) {
                        if (cell === col.displayName) {
                            return 'cell-edit';
                        }
                    }
                }

                return '';
            }
        };

        // only value column is allowed to be manipulated

        if (fullPropertyKey.startsWith(_dataRecordsGridConstants.get(this).propertyKeyPrefix) && !readOnly) {
            column.menuItems = this.addDropdownOptionsToColumns();
        }
        return column;
    }

    addDropdownOptionsToColumns() {
        let menuItems = [
            {
                title: 'Rename Column',
                icon: 'fa fa-pencil-square-o column-icon',
                action: this._renameColumn,
                context: this
            },
            {
                title: 'Delete Column',
                icon: 'ui-grid-icon-cancel',
                action: this._deleteColumn,
                context: this
            }
        ];
        return menuItems;
    }

    uniqueColumns(array) {
        let seen = {};
        return array.filter((item) => {
            return seen.hasOwnProperty(item.displayName) ? false : (seen[item.displayName] = true);
        });
    }

    _deleteColumn() {
        this.context.$rootScope.$broadcast('deleteColumnEvent', {
            columnField: this.context.col.field
        });
    }

    _renameColumn() {
        this.context.$rootScope.$broadcast('renameColumnEvent', {
            columnField: this.context.col.field
        });
    }

    _mapImportedDataForSaving(dataRows) {
        this.row = dataRows[0];

        this.dataObject = {
            values: []
        };
        this.dataObject.values = this.row;
        let keys = this.extractFieldKeys(this.dataObject, this.readOnly);
        let columns = [];
        this.addFieldColumns(keys, columns);
        this.columnNames = [];
        for (let col of columns) {
            this.columnNames.push(col.name);
        }

        this._extractDataValues(dataRows, this.columnNames);
        this._removeRedundantProperties(dataRows, this.columnNames);
        this._convertEmptyToNull(dataRows);
        this.data = this._generateRowIds(dataRows);
        return this.data;
    }

    _extractDataValues(rows, columnNames) {
        this.dataValues = [];
        for (let row of rows) {
            this.values = {};
            for (let columnName of columnNames) {
                this.values[columnName] = row[columnName];
            }
            this.dataValues.push(this.values);
        }
        let index = 0;
        for (let row of rows) {
            row.values = this.dataValues[index];
            index++;
        }
    }

    _removeRedundantProperties(rows, columnNames) {
        for (let row of rows) {
            for (let columnName of columnNames) {
                delete row[columnName];
            }
        }
    }

    _convertEmptyToNull(rows) {
        for (let row of rows) {
            let rowValues = row.values;
            for (let values in row.values) {
                if (rowValues[values].length === 0) {
                    rowValues[values] = null;
                }
            }
        }
    }

    _generateRowIds(rows) {
        let temp = 0;
        for (let row of rows) {
            let timestamp = Date.now();
            let counter = timestamp + temp;
            row.id = this.createDefaultRowObject(counter).id;
            temp++;
        }
        return rows;
    }
}
