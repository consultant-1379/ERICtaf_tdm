package com.ericsson.cifwk.tdm.infrastructure.mapping;

import com.ericsson.cifwk.tdm.api.model.Node;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.google.common.collect.Lists;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

import java.util.List;
import java.util.stream.Collectors;

public class DataSourcesToGroupsConverter extends CustomConverter<List<DataSourceIdentityEntity>, List<Node>> {

    private static final String ROOT_GROUP_NAME = "root";

    @Override
    public List<Node> convert(List<DataSourceIdentityEntity> entities, Type<? extends List<Node>> type) {
        Node root = new Node(ROOT_GROUP_NAME);
        root.setGroup(true);

        for (DataSourceIdentityEntity identity : entities) {
            List<String> packages = Lists.newArrayList(identity.getGroup().split("\\."));
            insert(root, packages, "", identity);
        }
        collapseEmptyGroups(root);
        return root.getChildren();
    }

    private static void collapseEmptyGroups(Node node) {
        if (node.isGroup()) {
            List<Node> childGroups = childGroups(node.getChildren());
            if (hasOnlyOneChildWhichIsAGroup(node, childGroups)) {
                Node group = childGroups.get(0);
                node.setName(node.getName() + "." + group.getName());
                node.setGroupName(group.getGroupName());
                node.setChildren(group.getChildren());
                collapseEmptyGroups(node);
            } else {
                for (Node child : childGroups) {
                    collapseEmptyGroups(child);
                }
            }
        }
    }

    private static boolean hasOnlyOneChildWhichIsAGroup(final Node node, final List<Node> childGroups) {
        return childGroups.size() == 1 && node
                .getChildren()
                .size() == childGroups.size() && !ROOT_GROUP_NAME.equals(node.getName());
    }

    private static List<Node> childGroups(List<Node> nodes) {
        return nodes.stream()
                .filter(Node::isGroup)
                .collect(Collectors.toList());
    }

    private static void insert(Node root, List<String> currentPackage, String group, DataSourceIdentityEntity
            identity) {
        if (currentPackage.isEmpty()) {
            createDatasource(root, identity);
            return;
        }
        String current = currentPackage.remove(0);
        String fullyQualifiedGroup;
        if (group.isEmpty()) {
            fullyQualifiedGroup = current;
        } else {
            fullyQualifiedGroup = group + "." + current;
        }
        Node nodeGroup = getOrCreateGroup(root, current, fullyQualifiedGroup);
        insert(nodeGroup, currentPackage, fullyQualifiedGroup, identity);
    }

    private static Node getOrCreateGroup(Node node, String name, String fullyQalified) {
        List<Node> children = node.getChildren();
        for (Node child : children) {
            if (child.isGroup() && child.getName().equals(name)) {
                return child;
            }
        }
        Node newGroup = new Node(name);
        newGroup.setGroup(true);
        newGroup.setGroupName(fullyQalified);
        children.add(newGroup);
        return newGroup;
    }

    private static void createDatasource(Node node, DataSourceIdentityEntity identityEntity) {
        List<Node> children = node.getChildren();

        Node dataSource = new Node(identityEntity.getName());
        dataSource.setGroupName(identityEntity.getGroup());
        dataSource.setId(identityEntity.getId());
        dataSource.setCreatedBy(identityEntity.getCreatedBy());
        dataSource.setCreateTime(identityEntity.getCreateTime());
        dataSource.setGroup(false);

        children.add(dataSource);
    }
}
