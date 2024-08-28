const _scope = new WeakMap();
const _modalInstance = new WeakMap();

export default class CsvImportModalController {
    constructor($uibModalInstance, $scope) {
        'ngInject';

        _scope.set(this, $scope);
        _modalInstance.set(this, $uibModalInstance);
        this.errors = [];
    }

    addFileToTable() {
        this.resetErrors();
        this.file = document.getElementById('file').files[0];

        if (typeof this.file != 'undefined') {
            this.fileType = this.file.name.split('.')[1];

            if (this.fileType !== 'csv') {
                this.errors.push(this.fileType + ' files are not supported. ' +
                    'Only csv files can be uploaded.');
            }
            if (this.file.size > 10000000) {
                this.errors.push('The file size is ' + this.file.size + 'Bytes and ' +
                    'exceeds the maximum allowable file size of 10MB.');
            }
            if (this.errors.length === 0) {
                _modalInstance.get(this).close(this.file);
            }
        } else {
            this.errors.push('No file has been selected');
        }
    }

    closeAction() {
        _modalInstance.get(this).dismiss('cancel');
    }

    resetErrors() {
        this.errors = [];
    }
}
