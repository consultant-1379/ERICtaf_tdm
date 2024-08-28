const _contextResource = new WeakMap();
const _uibModalInstance = new WeakMap();
const _state = new WeakMap();

export default class ApprovalRequestModalController {
    constructor($state, contextId, session, contextResource, $uibModalInstance) {
        'ngInject';

        _contextResource.set(this, contextResource);
        _uibModalInstance.set(this, $uibModalInstance);
        _state.set(this, $state);

        this.contextId = contextId;
        this.username = session.name;
        this.displayProperty = 'summary';
        this.selected = [];
        this.comment = '';
    }

    loadReviewers(query) {
        return _contextResource.get(this)
            .users({id: this.contextId, query: query})
            .$promise.then((result) => {
                if (result.length === 0) {
                    let user1 = {username: 'No user found'};
                    result.push(user1);
                }
                return result.map(this._prepareAutocompleteField.bind(this));
            });
    }

    _prepareAutocompleteField(user) {
        if (user.email) {
            user[this.displayProperty] = user.username + ' - ' + user.email;
        } else {
            user[this.displayProperty] = user.username;
        }
        return user;
    }

    checkTag(tag) {
        if (tag.username === 'No-user-found') {
            return false;
        }
        return true;
    }

    canSend() {
        return this.selected.length > 0;
    }

    sendRequest() {
        _uibModalInstance.get(this).close({selected: this.selected, comment: this.comment});
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
