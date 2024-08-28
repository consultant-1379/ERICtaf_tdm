export default class AuthResolver {
    constructor($q, $rootScope, $state, authService, $location, $cookieStore) {
        'ngInject';

        this.$q = $q;
        this.$rootScope = $rootScope;
        this.$state = $state;

        this.authService = authService;

        this.lastState = {name: 'login'};
        this.toParams = null;
        this.$location = $location;
        this.$cookieStore = $cookieStore;
    }

    listenForStateChange() {
        this.$rootScope.$on('$stateChangeStart', (event, toState, toParams) => {
            this.lastState = toState;
            this.lastParams = toParams;

            if (this.$location.url() !== '/login') {
                this.$cookieStore.put('returnUrl', this.$location.url());
            }

        });
    }

    resolve() {
        let deferFactory = this.$q.defer();
        let to = encodeURIComponent(this.$state.href(this.lastState.name, this.lastParams));
        this.authService.checkCurrentUser().then((res) => {
            if (res.data.authenticated) {
                deferFactory.resolve();
            } else {
                deferFactory.reject();

                if (this.lastState.name !== 'login') {
                    this.redirect('login', {to});
                }
            }
        });
        return deferFactory.promise;
    }

    redirect(stateName, params) {
        this.$state.go(stateName, params, {location: 'replace'});
    }
}
