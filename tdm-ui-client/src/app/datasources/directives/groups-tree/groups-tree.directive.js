export default function() {
    return {
        restrict: 'E',
        templateUrl: 'app/datasources/directives/groups-tree/groups-tree.html',
        scope: {
            groupsData: '=',
            onSelect: '&',
            selectedGroupName: '=',
            selectedDatasource: '=',
            selectedContext: '='
        },
        controller: 'GroupsTreeController',
        controllerAs: 'vm',
        bindToController: true
    };
}
