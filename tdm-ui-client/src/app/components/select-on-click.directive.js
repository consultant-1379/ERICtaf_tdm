export default function() {
    return {
        restrict: 'A',
        link: function(scope, element) {
            element.on('click', () => {
                document.execCommand('selectAll', false, null);
            });
        }
    };
}
