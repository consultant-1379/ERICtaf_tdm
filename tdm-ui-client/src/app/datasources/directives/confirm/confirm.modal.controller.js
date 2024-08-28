const _modalInstance = new WeakMap();
const defaultMessage = 'Do you really want to proceed?';

export default class ConfirmModalController {
    constructor($uibModalInstance, onConfirm, message, secondMessage) {
        'ngInject';

        _modalInstance.set(this, $uibModalInstance);

        this.onConfirm = onConfirm;
        this.message = message ? message : defaultMessage;
        this.secondMessage = secondMessage ? secondMessage : '';
    }

    confirm() {
        this.onConfirm();
        _modalInstance.get(this).close();
    }

    cancel() {
        _modalInstance.get(this).dismiss('cancel');
    }
}
