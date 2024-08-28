export default class TreeNode {
    constructor(value, _parent) {
        'ngNoInject';
        this.value = value;
        this.children = [];

        this.getParent = function() {
            return _parent;
        };
        this.setParent = function(parent) {
            _parent = parent;
        };
    }

    addChild(child) {
        this.children.push(child);
        child.setParent(this);
    }

    remove() {
        let children = this.getParent().children;
        let index = children.indexOf(this);
        children.splice(index, 1);
        this.setParent(null);
    }
}
