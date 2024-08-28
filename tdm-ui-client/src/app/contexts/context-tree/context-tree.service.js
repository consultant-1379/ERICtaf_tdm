export default class ContextTreeService {
    constructor(treeNodeFactory) {
        'ngInject';
        this.treeNodeFactory = treeNodeFactory;
    }

    buildTree(models) {
        let modelsByParent = _.groupBy(models, 'parentId');
        let topModels = modelsByParent.null; // roots, where parentId was null
        return this.modelsToTree(topModels, modelsByParent);
    }

    modelsToTree(models, modelsByParent) {
        let tree = _(models)
            .sortBy('name')
            .map(model => {
                let node = this.treeNodeFactory.create(model);
                let childModels = modelsByParent[model.id] || [];
                let children = this.modelsToTree(childModels, modelsByParent);
                children.forEach(child => node.addChild(child));
                return node;
            })
            .value();
        return tree;
    }

    find(nodes, predicate) {
        for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i];
            if (predicate(node.value)) {
                return node;
            }
            let result = this.find(node.children, predicate);
            if (result != null) {
                return result;
            }
        }
    }

    addChild(parent, model) {
        let child = this.treeNodeFactory.create(model);
        parent.addChild(child);
        child.value.parentId = parent.value.id;
        return child;
    }

    delete(node) {
        node.remove();
        node.value.parentId = null;
        return node;
    }

    traceParents(node) {
        function traceParents(trace) {
            let firstNode = _.first(trace);
            let parent = firstNode.getParent();
            if (parent != null) {
                return traceParents([parent].concat(trace));
            } else {
                return trace;
            }
        }

        if (node == null) {
            return [];
        }
        return traceParents([node]);
    }
}
