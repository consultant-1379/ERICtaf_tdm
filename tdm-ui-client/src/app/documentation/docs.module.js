const moduleName = 'app.documentation';

import DocsController from './docs.controller';

angular
    .module(moduleName, ['hc.marked'])
    .controller('docsController', DocsController);

export default moduleName;
