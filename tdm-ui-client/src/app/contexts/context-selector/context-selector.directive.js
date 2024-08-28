export default function contextSelectorDirective() {
    return {
        restrict: 'E',
        templateUrl: 'app/contexts/context-selector/context-selector.html',
        scope: {
            selected: '=',
            contexts: '=',
            contextId: '@',
            onNodeClick: '&'
        },
        bindToController: true,
        controller: 'contextSelectorController',
        controllerAs: 'vm'
    };
}
