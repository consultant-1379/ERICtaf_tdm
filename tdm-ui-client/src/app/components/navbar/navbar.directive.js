export default function() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/navbar/navbar.html',
        scope: {
            creationDate: '='
        },
        controller: 'NavbarController',
        controllerAs: 'vm',
        bindToController: true,
        replace: true
    };
}
