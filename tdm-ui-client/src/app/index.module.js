import coreModule from './core/core.module';
import configModule from './config/config.module';
import commonModule from './components/common.module';
import routingModule from './routing/routing.module';
import loginModule from './login/login.module';
import datasourcesModule from './datasources/datasources.module';
import contextsModule from './contexts/contexts.module';
import docsModule from './documentation/docs.module';
import dashboardModule from './dashboard/dashboard.module';

angular.module('app', [
    coreModule,
    configModule,
    commonModule,
    docsModule,
    routingModule,
    loginModule,
    datasourcesModule,
    contextsModule,
    dashboardModule
]);
