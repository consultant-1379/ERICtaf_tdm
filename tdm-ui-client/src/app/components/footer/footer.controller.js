export default class FooterController {
    constructor(applicationResource, API_ROOT) {
        'ngInject';

        this.date = new Date();
        this.application = applicationResource.get();
        this.restApiUrl = API_ROOT + '/swagger-ui.html';
    }
}
