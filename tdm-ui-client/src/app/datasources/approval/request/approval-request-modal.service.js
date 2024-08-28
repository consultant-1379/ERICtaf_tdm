const _uibModal = new WeakMap();

export default class ApprovalRequestModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(contextId) {
        return _uibModal.get(this).open({
            templateUrl: 'app/datasources/approval/request/approval-request-modal.html',
            controller: 'approvalRequestModalController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                contextId: function() {
                    return contextId;
                }
            }
        });
    }
}
