var assert = require('assert');
import ContextService from '../../../src/app/contexts/context.service'


let service
describe('Context Service', function(){
    beforeEach(function() {
            service = new ContextService();
        });
    describe('find by id', function(){
        it('should return null when context id doesn\'t exist', function(){
            let contexts = [{'id':'a'},{'id':'b'},{'id':'c'}]
            let context = 'd'
            assert.equal(service._findById(contexts, context), null);
        });
        it('should return context id when it exists', function(){
            let contexts = [{'id':'a'},{'id':'b'},{'id':'c'}]
            let context = 'c'
            assert.equal(service._findById(contexts, context).id, 'c');
        });
    });
});
