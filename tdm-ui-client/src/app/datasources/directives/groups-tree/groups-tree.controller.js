const _state = new WeakMap();
const _dataSourceResource = new WeakMap();

export default class GroupsTreeController {
    constructor($scope, $state, dataSourceResource) {
        'ngInject';

        _state.set(this, $state);
        _dataSourceResource.set(this, dataSourceResource);

        $scope.$on('contextChanged', this._onContextChanged.bind(this));
        $scope.$watch('vm.selectedDatasource', this._initNodeStates.bind(this));
        $scope.$watch('vm.groupsData', (newValue) => {
            this.gridApi.setRowData(newValue);
            this.gridApi.forEachNode(this._initNodeSelection.bind(this));
        }, true);

        let columnDefs = [
            {
                headerName: '',
                cellRenderer: {
                    renderer: 'group',
                    innerRenderer: this._innerCellRenderer.bind(this)
                }
            }
        ];

        this.tree = {
            columnDefs: columnDefs,
            rowSelection: 'single',
            rowData: this.groupsData,
            enableColResize: true,
            enableSorting: true,
            rowHeight: 24,
            getNodeChildDetails: this._getNodeChildDetails.bind(this),
            onRowClicked: this._onRowClicked.bind(this),
            onGridReady: this._onGridReady.bind(this),
            icons: {
                groupExpanded: '<i class="fa fa-minus-circle"/>',
                groupContracted: '<i class="fa fa-plus-circle"/>'
            }
        };
    }

    _onContextChanged(event, received) {
        this.gridApi.setRowData(received);
        this.gridApi.refreshView();
    }

    _getNodeChildDetails(rowItem) {
        if (rowItem.group) {
            return {
                group: true,
                expanded: this.selectedGroupName ? this.selectedGroupName.startsWith(rowItem.groupName) : false,
                children: rowItem.children
            };
        } else {
            return null;
        }
    }

    _innerCellRenderer(params) {
        if (!params.data.group) {
            let url = _state.get(this).href('base.contexts.datasources.view', {dataSourceId: params.data.id});
            let icon = '<i class="fa fa-external-link"></i>';
            let link = '<a href="' + url + '" target="_blank" ng-click="$event.stopPropagation()">' + icon + '</a>';
            return link + '&nbsp;' + params.data.name;
        } else {
            return params.data.name;
        }
    }

    _onRowClicked(params) {
        params.node.setSelected(true, true);
        params.event.target.scrollIntoView();
        this.onSelect({node: params.node.data});
    }

    _onGridReady() {
        this.gridApi = this.tree.api;
        this._initNodeStates();
    }

    _initNodeStates() {
        this.gridApi.deselectAll();
        if (!this.selectedGroupName) {
            this.gridApi.collapseAll();
        }
        this.gridApi.forEachNode(this._initNodeState.bind(this));
        this.gridApi.onGroupExpandedOrCollapsed();
    }

    _initNodeState(node) {
        this._initNodeSelection(node);
        this._initNodeExpansion(node);
    }

    _initNodeSelection(node) {
        if (this.selectedDatasource) {
            let selectedDatasource = node.data.id === this.selectedDatasource;
            node.setSelected(selectedDatasource, selectedDatasource);
        } else {
            let selectedGroup = node.data.group && node.data.groupName === this.selectedGroupName;
            node.setSelected(selectedGroup, selectedGroup);
        }
    }

    _initNodeExpansion(node) {
        if (node.group && this.selectedGroupName && this.selectedGroupName.startsWith(node.data.groupName)) {
            node.expanded = true;
        }
    }
}
