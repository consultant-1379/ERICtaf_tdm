const _contextResource = new WeakMap();

export default class ValidationService {
    constructor(contextResource) {
        'ngInject';
        _contextResource.set(this, contextResource);
        this.userValidation = false;
    }

    validateUser(contextId) {
        _contextResource.get(this).validateUser({id: contextId})
            .$promise.then((result) => {
                this.userValidation = result.validated;
            });
    }

    getUserValidation() {
        return this.userValidation;
    }
}
