const moduleName = 'app.dashboard';

import DashboardController from './dashboard.controller';
import DashboardResource from './dashboard-resource';

angular
    .module(moduleName, [])

    .controller('dashboardController', DashboardController)
    .service('dashboardResource', DashboardResource);

export default moduleName;
