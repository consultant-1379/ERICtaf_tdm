const moduleName = 'app.config';

import config from './config';

angular
    .module(moduleName, [])
    .config(config);

export default moduleName;
