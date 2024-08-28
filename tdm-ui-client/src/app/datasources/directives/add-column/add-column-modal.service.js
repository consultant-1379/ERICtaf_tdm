const _uibModal = new WeakMap();

export default class AddColumnModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(currentColumnNames) {
        return _uibModal.get(this).open({
            animation: true,
            templateUrl: 'app/datasources/directives/add-column/add-column-modal.html',
            controller: 'addColumnModalController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                currentColumnNames: function() {
                    return currentColumnNames;
                }
            }
        });
    }
}
