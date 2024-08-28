export default function($stateProvider, $urlRouterProvider) {
    'ngInject';

    $stateProvider
        .state('login', {
            url: '/login',
            templateUrl: 'app/login/login.html',
            controller: 'loginController as vm'
        })
        .state('base', {
            abstract: true,
            url: '/contexts',
            template: '<div ncy-breadcrumb></div><ui-view/>',
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                },
                contexts: function(contextResource) {
                    return contextResource.query();
                }
            }
        })
        .state('base.contexts', {
            url: '/{contextId}',
            controller: 'contextsController',
            ncyBreadcrumb: {
                label: '{{vm.context.path || \'\' }}'
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                }
            }
        })
        .state('base.contexts.datasources', {
            abstract: true,
            url: '/datasources',
            views: {
                '@': {
                    templateUrl: 'app/datasources/datasources.html',
                    controller: 'dataSourcesController as vm'
                }
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                },
                groups: function(dataSourceResource, contexts, $stateParams, contextService) {
                    return contexts.$promise.then((resolved) => {
                        if (!$stateParams.contextId) {
                            $stateParams.contextId = contextService.getRootContextId(contexts);
                        }
                        let context = contextService.getCurrentContextById(resolved, $stateParams.contextId);
                        return dataSourceResource.getGroups({context: context.id});
                    });
                },
                selectedContext: function(contextResolver, contexts) {
                    return contextResolver.resolve(contexts);
                },
                contextList: function(contexts) {
                    return contexts;
                }
            }
        })
        .state('base.contexts.datasources.list', {
            url: '?group',
            views: {
                '@base.contexts.datasources': {
                    templateUrl: 'app/datasources/list/datasource-list-view.html',
                    controller: 'dataSourceListController as vm'
                }
            },
            reloadOnSearch: false,
            ncyBreadcrumb: {
                label: 'Data Sources'
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                }
            }
        })
        .state('base.contexts.datasources.create', {
            url: '/create',
            views: {
                '@': {
                    templateUrl: 'app/datasources/create/create-datasource-view.html',
                    controller: 'dataSourceCreateController as vm'
                }
            },
            ncyBreadcrumb: {
                label: 'Create',
                parent: 'base.contexts.datasources.list'
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                },
                groupList: function(dataSourceResource, contexts, $stateParams, contextService) {
                    return contexts.$promise.then((resolved) => {
                        let context = contextService.getCurrentContextById(resolved, $stateParams.contextId);
                        return dataSourceResource.getGroups({context: context.id, view: 'LIST'});
                    });
                }
            }
        })
        .state('base.contexts.datasources.view', {
            url: '/{dataSourceId}',
            views: {
                '@base.contexts.datasources': {
                    templateUrl: 'app/datasources/view/view-datasource.html',
                    controller: 'dataSourceViewController as vm'
                }
            },
            resolve: {
                dataSourceIdentity: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.get({id: $stateParams.dataSourceId});
                },
                dataSourceRecords: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.getRecords({id: $stateParams.dataSourceId});
                },
                dataSourceVersions: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.getVersions({id: $stateParams.dataSourceId});
                }
            },
            ncyBreadcrumb: {
                label: '{{vm.dataSourceIdentity.name}}',
                parent: 'base.contexts.datasources.list'
            }
        })
        .state('base.contexts.datasources.view-version', {
            url: '/{dataSourceId}/version/{version}',
            views: {
                '@base.contexts.datasources': {
                    templateUrl: 'app/datasources/view/view-datasource.html',
                    controller: 'dataSourceViewController as vm'
                }
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                },
                dataSourceIdentity: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.getByVersion({id: $stateParams.dataSourceId, version: $stateParams.version});
                },
                dataSourceRecords: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.getRecordsForVersion({id: $stateParams.dataSourceId, version: $stateParams.version});
                },
                dataSourceVersions: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.getVersions({id: $stateParams.dataSourceId});
                }
            },
            ncyBreadcrumb: {
                label: '{{vm.dataSourceIdentity.name}}',
                parent: 'base.contexts.datasources.list'
            }
        })
        .state('base.contexts.datasources.view.edit', {
            url: '/edit',
            views: {
                '@': {
                    templateUrl: 'app/datasources/edit/edit-datasource-view.html',
                    controller: 'dataSourceEditController as vm'
                }
            },
            ncyBreadcrumb: {
                label: 'Edit'
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                },
                groupList: function(dataSourceResource) {
                    return dataSourceResource.getGroups({context: 'systemId-1', view: 'LIST'});
                }
            }
        })
        .state('base.contexts.datasources.view.review', {
            url: '/review',
            views: {
                '@': {
                    templateUrl: 'app/datasources/review/review-datasource-view.html',
                    controller: 'dataSourceViewController as vm'
                }
            },
            ncyBreadcrumb: {
                label: 'Review'
            },
            resolve: {
                auth(authResolver) {
                    return authResolver.resolve();
                },
                dataSourceIdentity: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.get({id: $stateParams.dataSourceId});
                },
                dataSourceRecords: function(dataSourceResource, $stateParams) {
                    return dataSourceResource.getRecords({id: $stateParams.dataSourceId, includeDeleted: true});
                }
            }
        })
        .state('dashboard', {
            url: '/dashboard',
            views: {
                '@': {
                    templateUrl: 'app/dashboard/dashboard.html',
                    controller: 'dashboardController as vm'
                }
            },
            ncyBreadcrumb: {
                label: 'Dashboard'
            },
            resolve: {
                dashboardUsers: function(dashboardResource) {
                    return dashboardResource.get();
                },
                dashboardBrowserDataSources: function(dashboardResource) {
                    return dashboardResource.getBrowserDataSources();
                },
                dashboardRestDataSources: function(dashboardResource) {
                    return dashboardResource.getRestDataSources();
                }
            }
        })
        .state('documentation', {
            url: '/documentation',
            views: {
                '@': {
                    templateUrl: 'app/documentation/docs.html',
                    controller: 'docsController as vm'
                },
                '@documentation': {
                    templateUrl: 'app/documentation/docs.template.html'
                }
            },
            ncyBreadcrumb: {
                label: 'Documentation'
            }
        })
        .state('documentation.overview', {
            url: '/overview',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Overview'
            }
        })
        .state('documentation.commons', {
            url: '/userinterface',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'User Interface'
            }
        })
        .state('documentation.tutorials', {
            url: '/tutorial',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Tutorials'
            }
        })
        .state('documentation.datasource-list', {
            url: '/datasource/list',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Data Sources'
            }
        })
        .state('documentation.datasource-view', {
            url: '/datasource/view',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'View Data Source'
            }
        })
        .state('documentation.datasource-create', {
            url: '/datasource/create',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Create Data Source'
            }
        })
        .state('documentation.datasource-edit', {
            url: '/datasource/edit',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Edit Data Source'
            }
        })
        .state('documentation.datasource-copy', {
            url: '/datasource/copy',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Copy Data Source'
            }
        })
        .state('documentation.datasource-approve', {
            url: '/datasource/approve',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Approve Data Source'
            }
        })
        .state('documentation.datasource-label', {
            url: '/datasource/label',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Add label to Data Source'
            }
        })
        .state('documentation.usage-in-taf-tests', {
            url: '/taf',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'Usage in TAF tests'
            }
        })
        .state('documentation.user-roles', {
            url: '/roles',
            templateUrl: 'app/documentation/docs.template.html',
            controller: 'docsController as vm',
            ncyBreadcrumb: {
                label: 'User Roles in TDM'
            }
        });
        // .state('documentation.test-data-orchestration', {
        //    url: '/taf/test/orchestration',
        //    templateUrl: 'app/documentation/docs.template.html',
        //    controller: 'docsController as vm',
        //    ncyBreadcrumb: {
        //        label: 'Test Data Orchestration'
        //    }
        // });

    $urlRouterProvider.otherwise('login');
}
