export default function dataRecordsGridDirective() {
    return {
        restrict: 'E',
        templateUrl: 'app/datasources/directives/datasources-grid/datasources-grid.html',
        controller: 'dataSourcesGridDirectiveController',
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            identities: '='
        }
    };
}
