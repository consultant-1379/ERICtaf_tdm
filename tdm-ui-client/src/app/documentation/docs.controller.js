export default class DocsController {
    constructor($scope, $state, $anchorScroll, $location, $timeout,
                applicationResource) {
        'ngInject';

        this.templateId = $state.current.name.slice('documentation.'.length) || 'overview';
        $scope.$on('$stateChangeSuccess', () => {
            if ($state.includes('documentation')) {
                $timeout(() => {
                    $anchorScroll();
                }, 100);
            }
        });

        $anchorScroll.yOffset = 50;
        this.scrollTo = (id) => {
            $location.hash(id ? id : null);
            $anchorScroll();
        };

        if ($state.includes('documentation.datasource-approve')) {
            this.integrations = applicationResource.getIntegrations();
        }
    }
}
