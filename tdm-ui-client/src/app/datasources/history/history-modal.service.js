const _uibModal = new WeakMap();

export default class HistoryModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(dataSourceId) {
        return _uibModal.get(this).open({
            animation: true,
            templateUrl: 'app/datasources/history/history-modal.html',
            controller: 'historyModalController',
            controllerAs: 'vm',
            windowClass: 'app-modal-window',
            resolve: {
                dataSourceId: function() {
                    return dataSourceId;
                }
            }
        });
    }
}
