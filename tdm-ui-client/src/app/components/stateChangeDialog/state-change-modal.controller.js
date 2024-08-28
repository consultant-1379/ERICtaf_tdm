const _uibModalInstance = new WeakMap();

export default class StateChangeModalController {
    constructor($uibModalInstance) {
        'ngInject';

        _uibModalInstance.set(this, $uibModalInstance);
    }

    ok() {
        _uibModalInstance.get(this).close(true);
    }

    cancel() {
        _uibModalInstance.get(this).dismiss('cancel');
    }

}
