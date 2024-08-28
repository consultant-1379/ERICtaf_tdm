const _dataSourceResource = new WeakMap();
const _modalInstance = new WeakMap();
const _state = new WeakMap();

export default class CopyDataSourceModalController {
    constructor($state, $uibModalInstance,
                dataSourceId, dataSourceName,
                contexts, currentContextId,
                versions, currentVersion,
                currentGroup, dataSourceResource) {
        'ngInject';

        _modalInstance.set(this, $uibModalInstance);
        _dataSourceResource.set(this, dataSourceResource);
        _state.set(this, $state);

        this.dataSourceId = dataSourceId;
        this.oldDataSourceName = dataSourceName;
        this.dataSourceName = dataSourceName;

        this.versions = versions;
        this.currentVersion = currentVersion;
        this.baseVersion = false;
        this.errorInDataSourceName = false;
        this.errorInGroup = false;

        contexts.$promise.then((resolved) => {
            this.contexts = resolved;
            this.currentContext = resolved.find(c => {
                return c.id === currentContextId;
            });

            this._getGroupsForContext(this.currentContext);
        });

        this.currentGroup = currentGroup;

        this.groupAutoCompleteInputChange = this._setGroupName.bind(this);
        this.groupAutoCompleteEntrySelected = this._selectGroup.bind(this);

        this.validateDataSourceName();

        this.errorInDataSourceNameMessage = 'DataSource name cannot be the same as original or empty';
    }

    copy() {
        if (this.dataSourceName.length > 0 && this.currentGroup.length > 0) {
            let dataSourceCopyRequest = {
                dataSourceId: this.dataSourceId,
                version: this.currentVersion.number,
                newName: this.dataSourceName.trim(),
                newContextId: this.currentContext.id,
                newGroup: this.currentGroup,
                baseVersion: this.baseVersion
            };
            _modalInstance.get(this).close(dataSourceCopyRequest);
        }
    }

    cancel() {
        _modalInstance.get(this).dismiss('cancel');
    }


    validateDataSourceName() {
        this.errorInDataSourceName = (this.dataSourceName.length === 0 ||
        this.oldDataSourceName === this.dataSourceName);
    }

    onContextChange(currentContext) {
        this._getGroupsForContext(currentContext);
    }

    _getGroupsForContext(context) {
        _dataSourceResource.get(this)
            .getGroups({context: context.id, view: 'LIST'}).$promise
            .then((resolvedGroups) => {
                this.groups = resolvedGroups.map(groupName => {
                    return {groupName: groupName};
                });
            });
    }

    _setGroupName(value) {
        this.currentGroup = value;
        this.errorInGroup = this.currentGroup.length === 0;
    }

    _selectGroup(selected) {
        if (selected) {
            this.currentGroup = selected.originalObject.groupName;
        }
    }

    goToHelp() {
        this._goTo('documentation.datasource-copy');
    }

    _goTo(state) {
        let url = _state.get(this).href(state);
        window.open(url, '_blank');
    }
}
