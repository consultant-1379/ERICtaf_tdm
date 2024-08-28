const moduleName = 'app.routing';

import config from './route.config';
import initBlock from './init.run';
import authEventsListener from './authevents.listener';
import AuthResolver from './auth-resolver';

angular
    .module(moduleName, [])
    .config(config)
    .service('authResolver', AuthResolver)
    .run(authEventsListener)
    .run(initBlock);

export default moduleName;
