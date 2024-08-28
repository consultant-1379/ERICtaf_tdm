const moduleName = 'app.login';

import LoginController from './login.controller';
import AuthServiceFactory from './authentication/authentication.service';
import SessionService from './authentication/session.service';
import PreferencesService from './preferences.service';

angular
    .module(moduleName, [])
    .controller('loginController', LoginController)
    .factory('authService', AuthServiceFactory)
    .service('session', SessionService)
    .service('preferences', PreferencesService);

export default moduleName;
