const _uibModal = new WeakMap();

export default class StateChangeModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open() {
        return _uibModal.get(this).open({
            templateUrl: 'app/components/stateChangeDialog/state-change-modal.html',
            controller: 'stateChangeDialogController',
            controllerAs: 'vm',
            size: 'sm'
        });
    }
}
