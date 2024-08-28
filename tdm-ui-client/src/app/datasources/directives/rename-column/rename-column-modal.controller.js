export default class RenameColumnModalController {
    constructor($uibModalInstance, columnName, currentColumnNames) {
        'ngInject';

        let editedColumnIndex = currentColumnNames.indexOf(columnName);
        currentColumnNames.splice(editedColumnIndex, 1);

        this.columnName = columnName;
        this.currentColumnNames = currentColumnNames;
        this.modalInstance = $uibModalInstance;

        this.invalidCharacters = new RegExp('[."@()\\\'^&$£%#~?\\/,{}\\[\\]\\\\+=]');
    }

    rename() {
        this.modalInstance.close(this.columnName);
    }

    cancel() {
        this.modalInstance.dismiss('cancel');
    }

    columnWithSuchNameExists() {
        return this.currentColumnNames.indexOf(this.columnName) > -1;
    }

    columnInvalidCharacters() {
        if (this.columnName) {
            return this.invalidCharacters.test(this.columnName);
        } else {
            return false;
        }
    }
}
