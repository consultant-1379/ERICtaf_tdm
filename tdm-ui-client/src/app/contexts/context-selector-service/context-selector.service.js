const _state = new WeakMap();

export default class ContextSelectorService {
    constructor($q, $aside, $rootScope, $state,
                applicationResource) {
        'ngInject';

        _state.set(this, $state);
        this.$q = $q;
        this.$rootScope = $rootScope;
        this.$aside = $aside;
        this.applicationResource = applicationResource;
    }

    openAside(contexts, contextId, caption = '') {
        return this.$q((resolve, reject) => {
            this.treeNode = null;
            let scope = this.$rootScope.$new(true);

            scope.integrations = this.applicationResource.getIntegrations();
            scope.selected = null;
            scope.contexts = contexts;
            scope.contextId = contextId;
            scope.caption = caption;
            scope.onNodeClick = (treeNode) => this.onNodeClick(treeNode);

            this.aside = this.$aside({
                scope,
                template: 'app/contexts/context-selector-service/context-selector-aside.html',
                animation: 'am-slide-left',
                placement: 'left',
                show: false
            });

            scope.$on('aside.hide', () => {
                if (this.treeNode == null) {
                    reject();
                } else {
                    let context = _.get(this.treeNode, 'value');
                    resolve(context);
                }
            });

            this.aside.$promise.then(() => {
                this.aside.show();
            });
        });
    }

    onNodeClick(treeNode) {
        if (treeNode != null) {
            this.treeNode = treeNode;
            this.aside.hide();
        }
    }

}

