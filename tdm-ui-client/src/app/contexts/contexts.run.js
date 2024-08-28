export default function runBlock(contextResolver) {
    'ngInject';

    contextResolver.listenForStateChange();
}
