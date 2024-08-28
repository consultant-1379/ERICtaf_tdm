export default class LoginController {
    constructor($rootScope, $state, authService, preferences, $location, $cookieStore) {
        'ngInject';

        this.$rootScope = $rootScope;
        this.$state = $state;
        this.authService = authService;
        this.preferences = preferences;
        this.$location = $location;
        this.$cookieStore = $cookieStore;

        this.credentials = {username: undefined, password: undefined};

        this.errorMessages = {
            credentials: false,
            unexpected: false,
            requiredCredentials: false,
            requiredUsername: false,
            requiredPassword: false
        };

        this.checkAuthentication();
    }

    loginSubmit(isValid) {
        this.loginSubmitted = true;
        if (isValid) {
            this.authService.login(this.credentials)
                .then((res) => {
                    this.$rootScope.isCustomerProfile = res.data.customerProfile;
                    this.loginSuccessful(res.data.username);
                },
                (res) => {
                    this.loginUnsuccessful(res);
                });
        } else {
            this.errorMessages.requiredUsername = this.credentials.username === undefined;
            this.errorMessages.requiredPassword = this.credentials.password === undefined;
            this.errorMessages.requiredCredentials =
                this.errorMessages.requiredUsername && this.errorMessages.requiredPassword;
        }
    }

    loginSuccessful(username) {
        this.resetForm();
        this.preferences.load(username).then((res) => {
            if (typeof this.$cookieStore.get('returnUrl') !== 'undefined' && this.$cookieStore.get('returnUrl') !== '') {
                this.$location.path(this.$cookieStore.get('returnUrl'));
                this.$cookieStore.remove('returnUrl');
            } else {
                this.$state.go('base.contexts.datasources.list', {contextId: res.data.contextId});
            }
        }, () => {
            this.$state.go('base.contexts');
        });
    }

    loginUnsuccessful(res) {
        this.loginError = true;
        if (res.status === 401) {
            this.errorMessages.credentials = true;
        } else {
            this.errorMessages.unexpected = true;
        }
    }

    clearLoginError() {
        this.loginError = false;
        this.errorMessages.credentials = false;
        this.errorMessages.unexpected = false;
        this.errorMessages.requiredCredentials = false;
        this.errorMessages.requiredUsername = false;
        this.errorMessages.requiredPassword = false;
    }

    resetForm() {
        this.credentials = {};
        this.loginSubmitted = false;
        this.loginError = null;
    }

    hasErrors() {
        return this.usernameHasError() || this.passwordHasError();
    }

    usernameHasError() {
        return (this.loginForm.username.$invalid || this.loginError) && this.loginSubmitted;
    }

    passwordHasError() {
        return (this.loginForm.password.$invalid || this.loginError) && this.loginSubmitted;
    }

    checkAuthentication() {
        this.authService.checkCurrentUser().then((res) => {
            if (res.data.authenticated) {
                this.$state.go('base.contexts.datasources.list');
            }
        });
    }
}
