const _state = new WeakMap();
const _scope = new WeakMap();
const _groups = new WeakMap();
const _dataSourceResource = new WeakMap();
const _contextResource = new WeakMap();
const _contextService = new WeakMap();
const _validationService = new WeakMap();

export default class DataSourcesController {
    constructor($state, $scope, $rootScope,
                groups, dataSourceResource,
                dataSourcesGridConstants, contextResource,
                contextList, contextService, validationService) {
        'ngInject';

        _state.set(this, $state);
        _scope.set(this, $scope);
        _groups.set(this, groups);
        _dataSourceResource.set(this, dataSourceResource);
        _contextResource.set(this, contextResource);
        _contextService.set(this, contextService);
        _validationService.set(this, validationService);

        $rootScope.$on('$stateChangeSuccess', (event, toState, toParams) => {
            this._parseStateParams(toParams);
        });

        $scope.$on(dataSourcesGridConstants.events.onDataSourceDelete, this._onDataSourceDeleteEvent.bind(this));

        this._initGroupTreeProperties();

        let stateParams = _state.get(this).params;

        this.context = _contextService.get(this).getCurrentContextById(contextList, stateParams.contextId);
        _validationService.get(this).validateUser(this.context.id);
    }

    _initGroupTreeProperties() {
        _groups.get(this).$promise.then((received) => {
            this.groupsData = received;
        });

        let params = _state.get(this).params;
        this._parseStateParams(params);
    }

    _parseStateParams(params) {
        if (params.dataSourceId) {
            _dataSourceResource.get(this).get({id: params.dataSourceId}).$promise.then((identity) => {
                this.selectedGroupName = identity.group;
                this.selectedDatasource = identity.id;
            });
        } else {
            this.selectedGroupName = params.group;
            this.selectedDatasource = params.dataSourceId;
        }
    }

    onGroupSelected(selectedGroup) {
        this.selectedGroupName = selectedGroup.groupName;
        this.selectedDatasource = selectedGroup.id;

        if (selectedGroup.group) {
            _state.get(this).go('base.contexts.datasources.list', {group: selectedGroup.groupName});
        } else {
            _state.get(this).go('base.contexts.datasources.view', {dataSourceId: selectedGroup.id});
        }

        _scope.get(this).$apply();
    }

    _onDataSourceDeleteEvent(event, groupToDelete) {
        let removedNodes = [];
        _deleteBranch(this.groupsData, groupToDelete.id);
        this._adjustSelectedGroup(_.first(removedNodes));

        function _deleteBranch(node, id) {
            if (_.isArray(node)) {
                _.remove(node, el => _deleteBranch(el, id))
                    .forEach(el => removedNodes.unshift(el));
                return node.length === 0;
            } else if (node.group) {
                return _deleteBranch(node.children, id);
            } else {
                return node.id === id;
            }
        }
    }

    _adjustSelectedGroup(removedNode) {
        if (this.selectedGroupName && removedNode.group) {
            let name = removedNode.name;
            let groupName = removedNode.groupName;
            let removedNodeParentGroupName = groupName.slice(0, -(name.length + 1));
            if (this.selectedGroupName.startsWith(removedNodeParentGroupName)) {
                this.selectedGroupName = removedNodeParentGroupName;
            }
        }
    }

    getUserValidation() {
        return _validationService.get(this).getUserValidation();
    }
}
