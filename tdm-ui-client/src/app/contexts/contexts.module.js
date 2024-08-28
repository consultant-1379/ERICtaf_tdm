const moduleName = 'app.contexts';

import ContextSelectorDirective from './context-selector/context-selector.directive';

import ContextSelectorController from './context-selector/context-selector.controller';

import ContextSelectorService from './context-selector-service/context-selector.service';
import ContextTreeService from './context-tree/context-tree.service.js';

import ContextResource from './context-resource';
import TreeNodeFactory from './context-tree/tree-node.factory.js';

import ContextService from './context.service.js';
import ContextsController from './contexts.controller';
import ContextResolver from './context-resolver';
import ContextInit from './contexts.run';

angular
    .module(moduleName, [])
    .directive('contextSelector', ContextSelectorDirective)

    .controller('contextSelectorController', ContextSelectorController)
    .controller('contextsController', ContextsController)

    .service('contextSelectorService', ContextSelectorService)
    .service('contextTreeService', ContextTreeService)
    .service('contextService', ContextService)

    .service('contextResource', ContextResource)
    .service('treeNodeFactory', TreeNodeFactory)
    .service('contextResolver', ContextResolver)
    .run(ContextInit);

export default moduleName;

