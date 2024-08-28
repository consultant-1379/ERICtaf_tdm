const _uibModal = new WeakMap();

export default class ConfirmModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(onConfirm, message, secondMessage) {
        return _uibModal.get(this).open({
            animation: true,
            templateUrl: 'app/datasources/directives/confirm/confirm.modal.html',
            controller: 'confirmModalController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                onConfirm: function() {
                    return onConfirm;
                },
                message: function() {
                    return message;
                },
                secondMessage: function() {
                    return secondMessage;
                }
            }
        }).result;
    }
}
