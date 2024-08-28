const _dataSourceEditConstants = new WeakMap();
const _logger = new WeakMap();

export default class EditActionsService {
    constructor(dataSourceEditConstants, logger) {
        'ngInject';

        _logger.set(this, logger);
        _dataSourceEditConstants.set(this, dataSourceEditConstants);
    }

    identityNameChangeAction(version, newValue) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityNameEdit,
            key: 'name',
            newValue: newValue,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    identityApprovalChangeAction(version, newValue) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityApprovalStatus,
            key: 'approvalStatus',
            newValue: newValue,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    identityGroupChangeAction(version, newValue) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityGroupEdit,
            key: 'group',
            newValue: newValue,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    identityVersionChangeAction(version, newValue) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityVersionEdit,
            key: 'version',
            newValue: newValue,
            oldValue: version,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    identityKeyAddAction(version, key) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityKeyAdd,
            key: key,
            newValue: null,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    identityKeyDeleteAction(version, key) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityKeyDelete,
            key: key,
            newValue: null,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    identityValueEditAction(version, key, newValue) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).identityValueEdit,
            key: key,
            newValue: newValue,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    addColumnAction(version, newPropertyKey) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).recordKeyAdd,
            key: newPropertyKey,
            newValue: null,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    changeColumnOrder(version, propertyKey, newOrder) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).columnOrderChange,
            key: propertyKey,
            newValue: newOrder,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    deleteColumnAction(version, propertyKey) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).recordKeyDelete,
            key: propertyKey,
            newValue: null,
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    renameColumnAction(version, newPropertyKey, oldPropertyKey) {
        let action = {
            id: null,
            version: version,
            type: _dataSourceEditConstants.get(this).recordKeyRename,
            key: oldPropertyKey,
            newValue: newPropertyKey,
            oldValue: oldPropertyKey,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    deleteRecordAction(version, record) {
        let action = {
            id: record.id,
            version: version,
            type: _dataSourceEditConstants.get(this).recordDelete,
            key: 'deleted',
            newValue: 'true',
            oldValue: null,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    addRecordAction(version, record, key, newValue, oldValue) {
        let action = {
            id: record.id,
            version: version,
            type: _dataSourceEditConstants.get(this).recordAdd,
            key: key,
            newValue: newValue,
            oldValue: oldValue,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }

    editRecordAction(version, record, key, newValue, oldValue) {
        let action = {
            id: record.id,
            version: version,
            type: _dataSourceEditConstants.get(this).recordValueEdit,
            key: key,
            newValue: newValue,
            oldValue: oldValue,
            localTimestamp: Date.now()
        };
        _logger.get(this).info('Action created', action);
        return action;
    }
}
