export default class ContextResolver {
    constructor($q, $state, $rootScope, contextService, preferences, authService) {
        'ngInject';

        this.$q = $q;
        this.$state = $state;
        this.$rootScope = $rootScope;
        this.contextService = contextService;
        this.preferences = preferences;
        this.authService = authService;

        this.lastParams = null;
    }

    listenForStateChange() {
        this.$rootScope.$on('$stateChangeStart', (event, toState, toParams) => {
            this.lastParams = toParams;
        });
    }

    resolve(contexts) {
        let df = this.$q.defer();

        contexts.$promise.then((resolved) => {
            let currentContext = this.contextService.getCurrentContextById(resolved, this.lastParams.contextId);
            if (currentContext) {
                df.resolve();
            } else {
                df.reject();
                let username = this.authService.session.name;
                this.preferences.load(username).then((preferences) => {
                    this.redirect('base.contexts.datasources.list', {contextId: preferences.data.contextId});
                }, () => {
                    this.redirect('base.contexts', {});
                });
            }
        }, () => {
            df.reject();
            this.redirect('base.contexts', {});
        });
        return df.promise;
    }

    redirect(stateName, params) {
        this.$state.go(stateName, params, {location: 'replace'});
    }
}
