export default function groupAutoCompleteInputDirective() {
    return {
        restrict: 'E',
        templateUrl: 'app/datasources/directives/group-autocomplete-input/group-autocomplete-input.html',
        controller: 'groupAutoCompleteInputDirectiveController',
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            initValue: '=',
            availableGroups: '=',
            onSelect: '=',
            onInputChange: '=',
            fieldName: '@',
            readOnly: '@'
        }
    };
}
