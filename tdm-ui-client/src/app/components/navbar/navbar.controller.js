const _state = new WeakMap();
const _rootScope = new WeakMap();
const _authService = new WeakMap();

export default class NavbarController {
    constructor($state, $rootScope, authService, AUTH_EVENTS) {
        'ngInject';

        _state.set(this, $state);
        _rootScope.set(this, $rootScope);
        _authService.set(this, authService);

        this.navCollapsed = true;
        this.AUTH_EVENTS = AUTH_EVENTS;
    }

    userName() {
        return _authService.get(this).session.name;
    }

    goToMain() {
        this._goTo('base.contexts.datasources.list');
    }

    goToHelp() {
        this._goToNewTab('documentation.overview');
    }

    goToDashboard() {
        this._goTo('dashboard');
    }

    signIn() {
        this._goTo('login');
    }

    _goTo(state) {
        _state.get(this).go(state);
    }

    _goToNewTab(state) {
        let url = _state.get(this).href(state);
        window.open(url, '_blank');
    }

    signOut() {
        _authService.get(this).signOut()
            .then(() => {
                _rootScope.get(this).$broadcast(this.AUTH_EVENTS.logoutSuccess);
            });
    }

    isLogin() {
        return _state.get(this).current.name === 'login';
    }

    isAuthenticated() {
        return _authService.get(this).isAuthenticated();
    }
}

