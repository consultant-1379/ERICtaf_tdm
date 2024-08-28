const _uibModal = new WeakMap();

export default class CopyDataSourceModalService {
    constructor($uibModal) {
        'ngInject';

        _uibModal.set(this, $uibModal);
    }

    open(dataSourceId, dataSourceName,
         contexts, currentContextId,
         versions, currentVersion,
         currentGroup
    ) {
        return _uibModal.get(this).open({
            animation: true,
            templateUrl: 'app/datasources/directives/copy-datasource/copy-datasource-modal.html',
            controller: 'copyDataSourceModalController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                dataSourceId: function() {
                    return dataSourceId;
                },
                dataSourceName: function() {
                    return dataSourceName;
                },
                contexts: function() {
                    return contexts;
                },
                currentContextId: function() {
                    return currentContextId;
                },
                versions: function() {
                    return versions;
                },
                currentVersion: function() {
                    return currentVersion;
                },
                currentGroup: function() {
                    return currentGroup;
                }
            }
        });
    }
}
