const _http = new WeakMap();
const _session = new WeakMap();

export default class PreferencesService {
    constructor($http, session, API_ROOT) {
        'ngInject';

        _http.set(this, $http);
        _session.set(this, session);

        this.API_ROOT = API_ROOT;
    }

    load(userId) {
        return _http.get(this).get(this.API_ROOT + '/preferences/' + userId);
    }

    save(context) {
        let preferences = {
            userId: _session.get(this).name,
            contextId: context.id
        };
        _http.get(this).put(this.API_ROOT + '/preferences', preferences);
    }
}
