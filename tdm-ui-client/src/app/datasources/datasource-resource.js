export default function($resource, API_ROOT) {
    'ngInject';

    return $resource(API_ROOT + '/datasources/:id', {id: '@id', label: '@label', contextId: '@contextId'}, {
        get: {
            method: 'get',
            timeout: 10000
        },
        delete: {
            method: 'delete',
            timeout: 10000
        },
        getByVersion: {
            url: API_ROOT + '/datasources/:id/versions/:version',
            method: 'get',
            timeout: 10000
        },
        create: {
            method: 'post',
            timeout: 10000,
            isArray: false
        },
        update: {
            method: 'patch',
            timeout: 10000,
            isArray: false
        },
        approve: {
            url: API_ROOT + '/datasources/:id/approval',
            method: 'post',
            timeout: 10000,
            isArray: false
        },
        copy: {
            url: API_ROOT + '/datasources/copy',
            method: 'post',
            timeout: 10000,
            isArray: false
        },
        getVersions: {
            url: API_ROOT + '/datasources/:id/versions',
            method: 'get',
            isArray: true,
            timeout: 10000
        },
        getRecords: {
            url: API_ROOT + '/datasources/:id/records',
            method: 'get',
            isArray: false,
            timeout: 10000
        },
        getRecordsForVersion: {
            url: API_ROOT + '/datasources/:id/versions/:version/records',
            method: 'get',
            isArray: false,
            timeout: 10000
        },
        getIdentities: {
            method: 'get',
            timeout: 10000,
            isArray: true
        },
        getGroups: {
            url: API_ROOT + '/datasources/groups',
            method: 'get',
            isArray: true,
            timeout: 10000
        },
        addLabel: {
            url: API_ROOT + '/datasources/labels',
            method: 'post',
            isArray: false,
            timeout: 10000
        },
        deleteLabel: {
            url: API_ROOT + '/datasources/labels/:label/contexts/:contextId',
            method: 'delete',
            isArray: false,
            timeout: 10000
        },
        getHistory: {
            url: API_ROOT + '/datasources/:id/history',
            method: 'get',
            isArray: true,
            timeout: 10000
        }
    });
}
