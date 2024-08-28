import TreeNode from './tree-node';

export default class TreeNodeFactory {
    create(value, parent) {
        return new TreeNode(value, parent);
    }
}
