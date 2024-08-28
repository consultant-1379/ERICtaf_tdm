export default function() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/footer/footer.html',
        controller: 'FooterController',
        controllerAs: 'vm',
        bindToController: true,
        replace: true
    };
}
