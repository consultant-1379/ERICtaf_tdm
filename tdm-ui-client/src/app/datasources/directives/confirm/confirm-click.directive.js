export default function(confirmModalService) {
    'ngInject';

    return {
        restrict: 'A',
        scope: {
            confirmClick: '&',
            confirmMessage: '@',
            confirmSecondMessage: '@'
        },
        link: function(scope, element) {
            element.bind('click', () => confirmModalService.open(scope.confirmClick, scope.confirmMessage, scope.confirmSecondMessage));
        }
    };
}
