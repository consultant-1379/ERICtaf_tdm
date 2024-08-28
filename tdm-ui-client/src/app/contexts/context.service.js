const _state = new WeakMap();

export default class ContextService {
    constructor($state) {
        'ngInject';

        _state.set(this, $state);
    }

    getCurrentContextById(contexts, id) {
        let contextId = id || _state.get(this).params.contextId;
        let context = this._findById(contexts, contextId);
        if (context) {
            context.path = this._getContextPath(contexts, context);
        }
        return context;
    }

    _findById(contexts, id) {
        return contexts.find((context) => {
            return context.id === id;
        });
    }

    getRootContextId(contexts) {
        if (contexts[0]) {
            return contexts[0].id;
        } else {
            return '';
        }
    }

    _getContextPath(contexts, current) {
        let contextName = current.name;
        if (current.parentId) {
            let parent = this._findParent(contexts, current.parentId);
            return this._getContextPath(contexts, parent) + ' / ' + contextName;
        } else {
            return contextName;
        }
    }

    _findParent(contexts, parentId) {
        return contexts.find((context) => {
            return context.id === parentId;
        });
    }
}
