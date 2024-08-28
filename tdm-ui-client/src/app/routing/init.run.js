export default function runBlock(authResolver) {
    'ngInject';

    authResolver.listenForStateChange();
}
