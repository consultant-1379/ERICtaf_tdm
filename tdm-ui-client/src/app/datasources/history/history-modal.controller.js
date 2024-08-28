const _dataSourceResource = new WeakMap();
const _uibModalInstance = new WeakMap();

export default class HistoryModalController {
    constructor(dataSourceId, dataSourceResource, $uibModalInstance) {
        'ngInject';

        _dataSourceResource.set(this, dataSourceResource);
        _uibModalInstance.set(this, $uibModalInstance);
        this.dataSourceId = dataSourceId;
        this.dataSourcesMetaData = [];
        this.columnHeadings = ['Status', 'Reviewer', 'Version', 'Date'];
    }

    getHistory() {
        _dataSourceResource.get(this).getHistory({id: this.dataSourceId})
            .$promise.then((result) => this.dataSourcesMetaData.data = [].concat(result));
    }

    hasVersions() {
        return _.isEmpty(this.dataSourcesMetaData);
    }

    cancel() {
        _uibModalInstance.get(this).dismiss('cancel');
    }
}
