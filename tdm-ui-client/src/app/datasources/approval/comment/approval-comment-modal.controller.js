const _uibModalInstance = new WeakMap();
const _state = new WeakMap();

export default class ApprovalCommentModalController {
    constructor($state, decision, approvalStatusConstants, $uibModalInstance) {
        'ngInject';

        this.decision = decision;
        this.approvalStatusConstants = approvalStatusConstants;
        this.comment = '';

        _uibModalInstance.set(this, $uibModalInstance);
        _state.set(this, $state);
    }

    isApprove() {
        return this.decision === this.approvalStatusConstants.APPROVED;
    }

    isUnApprove() {
        return this.decision === this.approvalStatusConstants.UNAPPROVED;
    }

    isReject() {
        return this.decision === this.approvalStatusConstants.REJECTED;
    }

    canSend() {
        return this.isApprove() || this.comment;
    }

    sendDecision() {
        _uibModalInstance.get(this).close(this.comment);
    }

    cancel() {
        _uibModalInstance.get(this).dismiss('cancel');
    }

    goToHelp() {
        this._goTo('documentation.datasource-approve');
    }

    _goTo(state) {
        let url = _state.get(this).href(state);
        window.open(url, '_blank');
    }
}
