export default function($resource, API_ROOT) {
    'ngInject';

    return $resource(API_ROOT + '/contexts/:id', {id: '@id'}, {
        get: {
            method: 'get',
            timeout: 10000
        },
        query: {
            url: API_ROOT + '/contexts',
            method: 'get',
            timeout: 10000,
            isArray: true
        },
        users: {
            url: API_ROOT + '/contexts/:id/users',
            method: 'get',
            timeout: 10000,
            isArray: true
        },

        validateUser: {
            url: API_ROOT + '/contexts/:id/validateUser',
            method: 'get',
            timeout: 10000
        }
    });
}
