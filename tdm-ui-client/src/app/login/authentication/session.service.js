export default class SessionService {
    constructor() {
        'ngInject';

        this.name = null;
        this.authenticated = false;
        this.roles = [];
    }

    create(name, authenticated, roles) {
        this.name = name;
        this.authenticated = authenticated;
        this.roles = roles;
    }

    destroy() {
        this.name = null;
        this.authenticated = false;
        this.roles = [];
    }

    hasRole(role) {
        return this.roles.indexOf(role) > -1;
    }
}
