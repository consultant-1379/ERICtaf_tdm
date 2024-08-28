export default function($state, $rootScope, AUTH_EVENTS) {
    'ngInject';

    $rootScope.$on(AUTH_EVENTS.logoutSuccess, (event) => {
        event.preventDefault();
        if (!$state.includes('documentation') || !$state.includes('dashboard')) {
            $state.go('login');
        }
    });
}
