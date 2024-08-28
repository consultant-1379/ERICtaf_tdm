class AuthService {
    constructor($http, session, API_ROOT) {
        'ngNoInject';

        this.$http = $http;
        this.session = session;
        this.API_ROOT = API_ROOT;
    }

    login(credentials) {
        return this.$http
            .post(this.API_ROOT + '/login', credentials)
            .then((res) => {
                this._createSession(res.data);
                return res;
            });
    }

    _createSession(userData) {
        this.session.create(userData.username, userData.authenticated, userData.roles);
    }

    signOut() {
        return this.$http
            .delete(this.API_ROOT + '/login')
            .then(() => {
                this.session.destroy();
            });
    }

    setAuthorized(isAuthorized) {
        this.session.authenticated = isAuthorized;
    }

    isAuthenticated() {
        return this.session.authenticated;
    }

    checkCurrentUser() {
        return this.$http
            .get(this.API_ROOT + '/login')
            .then((res) => {
                if (res.data.authenticated) {
                    this._createSession(res.data);
                }
                return res;
            }, (err) => {
                return err;
            });
    }
}

export default function AuthServiceFactory($http, session, API_ROOT) {
    'ngInject';
    return new AuthService($http, session, API_ROOT);
}
