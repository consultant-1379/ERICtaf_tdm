let dataRecordGridConstants = {
    valueFieldName: 'values',
    propertyKeyPrefix: 'values.',
    excludedFromRenderColumns: ['id', 'dataSourceId', 'emptyRow', 'values', 'deleted', 'modifiedColumns', 'oldValues'],
    events: {
        afterCellEdit: 'afterDataRecordEdit',
        onDataRecordDelete: 'onDataRecordDelete',
        onColumnAdd: 'onColumnAdd',
        onColumnDelete: 'onColumnDelete',
        onColumnRename: 'onColumnRename',
        onColumnMove: 'onColumnMove'
    }
};
export default dataRecordGridConstants;
