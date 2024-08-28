var assert = require('assert');
import DataSourceService from '../../../src/app/datasources/datasource.service'

let service
describe('Data Source Service.', function(){
    beforeEach(function() {
            service = new DataSourceService();
        });
    describe('increment Approved Version.', function(){
        it('should return same version when it\'s a snapshot already', function(){
            assert.equal(service.incrementApprovedVersion("0.0.1-SNAPSHOT"), "0.0.1-SNAPSHOT");
        });
        it('should return incremented snapshot version when version is approved', function(){
            assert.equal(service.incrementApprovedVersion("0.0.1"), "0.0.2-SNAPSHOT");
        });
        it('should handle errors in version format', function(){
            assert.equal(service.incrementApprovedVersion("not correct format."), "not correct format.");
        });
        it('should handle nulls', function(){
            assert.equal(service.incrementApprovedVersion(null), null);
        })
    });
    describe('validate data records before saving', function(){
        it('should return error message when there are no data records', function(){
            let dataRecords = [];
            assert.equal(service.validateDataRecords(dataRecords).message, 'The datasource is empty');
        });
        it('should return error message when there is an empty data record', function(){
            let dataRecord = {emptyRow: true};
            let dataRecords = [dataRecord];
            assert.equal(service.validateDataRecords(dataRecords).message, 'The datasource has at least one empty row');
        });
        it('should return error message when there is a data record where all cells are empty', function(){
            let dataRecord = {emptyRow: false, values: {"col1": "", "col2": ""}};
            let dataRecords = [dataRecord];
            assert.equal(service.validateDataRecords(dataRecords).message, 'The datasource has at least one empty row');
        });
        it('should not return error message when there are some empty cells in a data record', function(){
            let dataRecord = {emptyRow: false, values: {"col1": "", "col2": "hello"}};
            let dataRecords = [dataRecord];
            assert.equal(service.validateDataRecords(dataRecords), null);
        });
    });
});
