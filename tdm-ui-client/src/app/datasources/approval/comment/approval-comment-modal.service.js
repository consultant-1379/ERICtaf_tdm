const _uibModal = new WeakMap();

export default class ApprovalCommentModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(decision) {
        return _uibModal.get(this).open({
            templateUrl: 'app/datasources/approval/comment/approval-comment-modal.html',
            controller: 'approvalCommentModalController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                decision: function() {
                    return decision;
                }
            }
        });
    }
}
