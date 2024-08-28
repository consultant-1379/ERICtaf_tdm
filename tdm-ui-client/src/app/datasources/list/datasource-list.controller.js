const _state = new WeakMap();
const _contexts = new WeakMap();
const _scope = new WeakMap();
const _dataSourceResource = new WeakMap();
const _contextService = new WeakMap();
const _location = new WeakMap();
const _dataSourcesGridConstants = new WeakMap();

export default class DataSourceListController {
    constructor($state, $scope, dataSourceResource, dataSourcesGridConstants, contexts, contextService, $location) {
        'ngInject';

        _location.set(this, $location);
        _state.set(this, $state);
        _scope.set(this, $scope);
        _contexts.set(this, contexts);
        _dataSourceResource.set(this, dataSourceResource);
        _contextService.set(this, contextService);
        _dataSourcesGridConstants.set(this, dataSourcesGridConstants);
        $scope.$on(dataSourcesGridConstants.events.onDataSourceDelete, this._onDataSourceDeleteEvent.bind(this));

        this.selectContextMessage = 'Datasources are displayed for selected context and its children.';

        this._initIdentitiesByContext();
    }

    _initIdentitiesByContext() {
        let stateParams = _state.get(this).params;

        _contexts.get(this).$promise.then((contexts) => {
            this.context = _contextService.get(this).getCurrentContextById(contexts, stateParams.contextId);

            _dataSourceResource.get(this).getIdentities({context: this.context.id}).$promise.then((identities) => {
                this.dataSourceIdentities = identities;
                this.filteredDataSourceIdentities = [].concat(this.dataSourceIdentities);

                _scope.get(this).$watch('$parent.vm.selectedGroupName', (newGroupName) => {
                    if (newGroupName) {
                        this._onSelectGroup(newGroupName);
                    } else {
                        _location.get(this).search('group', null);
                        this.reload(this.dataSourceIdentities, this.filteredDataSourceIdentities);
                    }
                });
            });
        });
    }

    reload(received, groups) {
        while (groups.length > 0) {
            groups.pop();
        }
        for (let group of received) {
            groups.push(group);
        }
        if (!(_scope.get(this).$$phase === '$digest')) {
            _scope.get(this).$apply();
        }
    }

    _onSelectGroup(selectedGroupName) {
        let clearGridRows = _dataSourcesGridConstants.get(this).events.onClearGridRows;
        _scope.get(this).$broadcast(clearGridRows);

        _location.get(this).search('group', selectedGroupName);
        for (let identity of this.dataSourceIdentities) {
            if (identity.group.startsWith(selectedGroupName)) {
                let index = this._indexOf(identity, this.filteredDataSourceIdentities);
                if (index < 0) {
                    this.filteredDataSourceIdentities.splice(index, 0, identity);
                }
            }
        }
        for (let i = this.filteredDataSourceIdentities.length - 1; i >= 0; i--) {
            if (!this.filteredDataSourceIdentities[i].group.startsWith(selectedGroupName)) {
                this.filteredDataSourceIdentities.splice(i, 1);
            }
        }
        if (!(_scope.get(this).$$phase === '$digest')) {
            _scope.get(this).$apply();
        }
    }

    _onDataSourceDeleteEvent(event, recordToDelete) {
        this._deleteFromIdentities(recordToDelete.id, this.dataSourceIdentities);
        this._deleteFromIdentities(recordToDelete.id, this.filteredDataSourceIdentities);
    }

    _deleteFromIdentities(id, identities) {
        for (let i = 0; i < identities.length; i++) {
            if (identities[i].id === id) {
                identities.splice(i, 1);
                return;
            }
        }
    }

    _indexOf(identity, identities) {
        for (let i = 0; i < identities.length; i++) {
            if (identities[i].id === identity.id) {
                return i;
            }
        }
        return -1;
    }
}
