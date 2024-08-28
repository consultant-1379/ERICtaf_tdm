/* global agGrid, angular */
const moduleName = 'app.core';

agGrid.initialiseAgGridWithAngular1(angular);

angular.module(moduleName, [

    /*
     * Angular modules
     */
    'ngAnimate', 'ngCookies',
    'ngTouch', 'ngSanitize',
    'ngMessages', 'ngResource',

    /*
     * 3PP
     */
    'ui.router', 'ui.bootstrap',
    'toastr',
    'angucomplete-alt',
    'ncy-angular-breadcrumb',
    'agGrid', 'mgcrea.ngStrap',
    'ui.tree', 'ngTagsInput',
    'xeditable', 'nvd3',


    /*
     * UI GRID
     */
    'ui.grid',
    'ui.grid.edit',
    'ui.grid.cellNav',
    'ui.grid.selection',
    'ui.grid.importer',
    'ui.grid.exporter',
    'ui.grid.resizeColumns',
    'ui.grid.moveColumns'

]).constant('AUTH_EVENTS', {
    loginSuccess: 'auth-login-success',
    loginFailed: 'auth-login-failed',
    logoutSuccess: 'auth-logout-success',
    sessionTimeout: 'auth-session-timeout',
    notAuthenticated: 'auth-not-authenticated',
    notAuthorized: 'auth-not-authorized'
}).constant('API_ROOT', '/api');

export default moduleName;
