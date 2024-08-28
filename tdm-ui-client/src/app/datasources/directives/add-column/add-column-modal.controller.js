export default class AddColumnModalController {

    constructor($uibModalInstance, $scope, currentColumnNames) {
        'ngInject';

        this.modalInstance = $uibModalInstance;
        this.currentColumnNames = currentColumnNames;
        this._scope = $scope;
        this.newColumnName = null;

        this.invalidCharacters = new RegExp('[."@()\\\'^&$£%#~?\\/,{}\\[\\]\\\\+=]');
    }

   addColumn() {
       this.modalInstance.close(this.newColumnName);
   }

   columnWithSuchNameExists() {
       return this.currentColumnNames.indexOf(this.newColumnName) > -1;
   }

   columnInvalidCharacters() {
       if (this.newColumnName) {
           return this.invalidCharacters.test(this.newColumnName);
       } else {
           return false;
       }
   }

}
