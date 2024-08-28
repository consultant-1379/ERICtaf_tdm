const moduleName = 'app.datasources';

import DataSourceListController from './list/datasource-list.controller';
import DataSourceCreateController from './create/create-datasource.controller';
import DataSourceResource from './datasource-resource';
import DataSourceService from './datasource.service';
import DataSourcesController from './datasources.controller';

import RenameColumnModalController from './directives/rename-column/rename-column-modal.controller';
import RenameColumnModalService from './directives/rename-column/rename-column-modal.service';

import confirmClickDirective from './directives/confirm/confirm-click.directive';
import ConfirmModalService from './directives/confirm/confirm.modal.service';
import ConfirmModalController from './directives/confirm/confirm.modal.controller';

import CopyDataSourceModalController from './directives/copy-datasource/copy-datasource-modal.controller';
import CopyDataSourceModalService from './directives/copy-datasource/copy-datasource-modal.service';

import DataSourceEditController from './edit/edit-datasource.controller';
import DataSourceEditConstants from './edit/edit-datasource.constants';
import EditActionsService from './edit/edit-actions.service';

import DataSourceViewController from './view/view-datasource.controller';
import ApprovalStatusConstants from './approval/approval-status.constants';
import ApprovalRequestModalService from './approval/request/approval-request-modal.service';
import ApprovalRequestModalController from './approval/request/approval-request-modal.controller';
import ApprovalCommentModalService from './approval/comment/approval-comment-modal.service';
import ApprovalCommentModalController from './approval/comment/approval-comment-modal.controller';

import GroupAutoCompleteInputDirectiveController from './directives/group-autocomplete-input/group-autocomplete-input.controller';
import GroupAutoCompleteInputDirective from './directives/group-autocomplete-input/group-autocomplete-input.directive';

import DataRecordsGridDirectiveController from './directives/data-records-grid/data-records-grid.controller';
import DataRecordsGridDirective from './directives/data-records-grid/data-records-grid.directive';
import DataRecordsGridConstants from './directives/data-records-grid/data-records-grid.constants';

import DataSourcesGridDirectiveController from './directives/datasources-grid/datasources-grid.controller';
import DataSourcesGridDirective from './directives/datasources-grid/datasources-grid.directive';
import DataSourcesGridConstants from './directives/datasources-grid/datasources-grid.constants';

import GroupsTreeController from './directives/groups-tree/groups-tree.controller.js';
import GroupsTreeDirective from './directives/groups-tree/groups-tree.directive.js';

import CsvImportModalController from './import/csv-import-modal.controller';

import AddColumnModalController from './directives/add-column/add-column-modal.controller';
import AddColumnModalService from './directives/add-column/add-column-modal.service';

import ValidationService from './directives/validation/validation.service';

import HistoryModalController from './history/history-modal.controller';
import HistoryModalService from './history/history-modal.service';

angular
    .module(moduleName, [])

    .controller('dataSourceListController', DataSourceListController)
    .controller('dataSourceCreateController', DataSourceCreateController)
    .service('dataSourceResource', DataSourceResource)
    .service('dataSourceService', DataSourceService)
    .controller('dataSourcesController', DataSourcesController)

    .controller('dataSourceEditController', DataSourceEditController)
    .constant('dataSourceEditConstants', DataSourceEditConstants)
    .service('editActionsService', EditActionsService)

    .controller('dataSourceViewController', DataSourceViewController)
    .constant('approvalStatusConstants', ApprovalStatusConstants)
    .service('approvalRequestModalService', ApprovalRequestModalService)
    .controller('approvalRequestModalController', ApprovalRequestModalController)
    .service('approvalCommentModalService', ApprovalCommentModalService)
    .controller('approvalCommentModalController', ApprovalCommentModalController)

    .directive('groupsTree', GroupsTreeDirective)
    .controller('GroupsTreeController', GroupsTreeController)

    .controller('renameColumnModalController', RenameColumnModalController)
    .service('renameColumnModalService', RenameColumnModalService)

    .directive('confirmClick', confirmClickDirective)
    .service('confirmModalService', ConfirmModalService)
    .controller('confirmModalController', ConfirmModalController)

    .controller('copyDataSourceModalController', CopyDataSourceModalController)
    .service('copyDataSourceModalService', CopyDataSourceModalService)

    .controller('groupAutoCompleteInputDirectiveController', GroupAutoCompleteInputDirectiveController)
    .directive('groupAutoCompleteInput', GroupAutoCompleteInputDirective)

    .controller('dataRecordsGridDirectiveController', DataRecordsGridDirectiveController)
    .directive('datarecordsGrid', DataRecordsGridDirective)
    .constant('dataRecordsGridConstants', DataRecordsGridConstants)

    .controller('dataSourcesGridDirectiveController', DataSourcesGridDirectiveController)
    .directive('datasourcesGrid', DataSourcesGridDirective)
    .constant('dataSourcesGridConstants', DataSourcesGridConstants)

    .controller('csvImportModalController', CsvImportModalController)

    .controller('addColumnModalController', AddColumnModalController)
    .service('addColumnModalService', AddColumnModalService)

    .service('validationService', ValidationService)

    .controller('historyModalController', HistoryModalController)
    .service('historyModalService', HistoryModalService);

export default moduleName;
