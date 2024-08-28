import footerDirective from './footer/footer.directive';
import navbarDirective from './navbar/navbar.directive';
import selectOnClickDirective from './select-on-click.directive';
import contentEditableDirective from './content-editable.directive';

import footerController from './footer/footer.controller';
import navbarController from './navbar/navbar.controller';

import stateChangeDialogController from './stateChangeDialog/state-change-modal.controller';
import stateChangeDialogService from './stateChangeDialog/state-change-modal.service';

import applicationResource from './application.resource';
import loggerService from './logger';

const moduleName = 'app.common';

angular.module(moduleName, [])
    .directive('footer', footerDirective)
    .directive('selectOnClick', selectOnClickDirective)
    .directive('navbar', navbarDirective)
    .directive('contenteditable', contentEditableDirective)

    .controller('FooterController', footerController)
    .controller('NavbarController', navbarController)
    .controller('stateChangeDialogController', stateChangeDialogController)

    .service('applicationResource', applicationResource)
    .service('stateChangeDialogService', stateChangeDialogService)
    .service('logger', loggerService);

export default moduleName;
