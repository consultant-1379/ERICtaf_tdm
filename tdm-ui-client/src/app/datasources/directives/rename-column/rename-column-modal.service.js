const _uibModal = new WeakMap();

export default class RenameColumnModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(columnName, currentColumnNames) {
        return _uibModal.get(this).open({
            animation: true,
            templateUrl: 'app/datasources/directives/rename-column/rename-column-modal.html',
            controller: 'renameColumnModalController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                columnName: function() {
                    return columnName;
                },
                currentColumnNames: function() {
                    return currentColumnNames;
                }
            }
        });
    }
}
