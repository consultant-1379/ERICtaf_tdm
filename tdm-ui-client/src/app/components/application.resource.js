export default function($resource, API_ROOT) {
    'ngInject';

    return $resource(API_ROOT + '/application', {}, {
        get: {method: 'get'},
        getIntegrations: {
            method: 'get',
            url: API_ROOT + '/application/integrations'
        }
    });
}

