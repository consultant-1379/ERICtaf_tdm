export default function dataRecordsGridDirective() {
    return {
        restrict: 'E',
        templateUrl: 'app/datasources/directives/data-records-grid/data-records-grid.html',
        controller: 'dataRecordsGridDirectiveController',
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            options: '=',
            table: '=',
            error: '=?',
            readOnly: '=',
            approvalStatus: '='
        },
        link: function(scope, element, attrs) {
            if (!attrs.ngIf) {
                throw new Error('ngIf property is mandatory. Directive need to be initialized when property is set.');
            }
            if (!attrs.options) {
                throw new Error('datarecords-grid-options property is mandatory');
            }
            if (attrs.options.data !== undefined) {
                throw new Error('options.data property is mandatory');
            }
            if (attrs.options.columnDefs !== undefined) {
                throw new Error('options.columnDefs property is mandatory');
            }
        }
    };
}
