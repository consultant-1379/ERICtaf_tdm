const _state = new WeakMap();
const _contextSelectorService = new WeakMap();
const _contextService = new WeakMap();
const _preferences = new WeakMap();

export default class ContextsController {
    constructor($state,
                contextSelectorService, contexts,
                contextService, preferences) {
        'ngInject';

        contexts.$promise.then((resolvedContexts) => {
            this.openContextSideMenu(resolvedContexts);
        });
        _state.set(this, $state);
        _contextSelectorService.set(this, contextSelectorService);
        _contextService.set(this, contextService);
        _preferences.set(this, preferences);
    }

    openContextSideMenu(contexts) {
        let currentContext = _contextService.get(this).getCurrentContextById(contexts);
        let promise = _contextSelectorService.get(this)
            .openAside(
                contexts,
                currentContext ? currentContext.id : null,
                this.selectContextMessage);
        this.resolveContextSelection(promise);
    }

    resolveContextSelection(contextPromise) {
        contextPromise
            .then(context => {
                _preferences.get(this).save(context);
                _state.get(this).go('base.contexts.datasources.list', {contextId: context.id});
                return context;
            }, () => {
                _state.get(this).go('base.contexts.datasources.list');
            });
    }
}
