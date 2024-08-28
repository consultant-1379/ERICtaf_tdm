export default function($resource, API_ROOT) {
    'ngInject';

    return $resource(API_ROOT + '/statistics/users', {}, {
        get: {
            method: 'get',
            timeout: 10000,
            isArray: true
        },

        getBrowserDataSources: {
            url: API_ROOT + '/statistics/dataSources',
            method: 'get',
            timeout: 10000,
            isArray: true
        },

        getRestDataSources: {
            url: API_ROOT + '/statistics/dataSources?type=Rest',
            method: 'get',
            timeout: 10000,
            isArray: true
        }

    });
}
