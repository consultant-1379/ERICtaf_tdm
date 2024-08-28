const _state = new WeakMap();
const _contextTreeService = new WeakMap();

export default class ContextSelectorController {
    constructor($state, $scope, contextTreeService) {
        'ngInject';

        _state.set(this, $state);
        _contextTreeService.set(this, contextTreeService);

        this.tree = [];
        this.selected = null;

        this.loadTree();

        $scope.$watch('vm.contextId', contextId => {
            // Only if already initialized
            if (this.selected != null) {
                this.selectById(contextId);
            }
        });
    }

    loadTree() {
        this.tree = _contextTreeService.get(this).buildTree(this.contexts);
        this.selectById(this.contextId);
    }

    selectById(id) {
        let currentContextId = _.get(this.selected, 'value.id');

        if (id == null) {
            this.selectRoot();
        } else if (id !== currentContextId || currentContextId == null) {
            let selected = _contextTreeService.get(this).find(this.tree, value => value.id === id);
            if (selected != null) {
                this.selectContext(selected);
            } else {
                this.selectRoot();
            }
        }
    }

    toggleNode(node) {
        if (node.$modelValue.children.length > 0) {
            node.toggle();
        }
    }

    selectNode(node) {
        this.selectContext(node.$modelValue);
    }

    selectContext(treeNode) {
        let parents = _contextTreeService.get(this).traceParents(treeNode);
        parents.pop(); // remove for model
        this.trace = _(parents)
            .map('value')
            .keyBy('id')
            .value();

        this.selected = treeNode;
    }

    selectRoot() {
        this.selectContext(this.tree[0]);
    }

    clickNode(node) {
        this.selectNode(node);
        this.onNodeClick({treeNode: node.$modelValue});
    }
}
