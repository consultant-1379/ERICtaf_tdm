/*eslint-disable*/
const _state = new WeakMap();
const _scope = new WeakMap();
const _dashboardUsers = new WeakMap();
const _dashboardBrowserDataSources = new WeakMap();
const _dashboardRestDataSources = new WeakMap();

export default class DashboardController {
    constructor($state, $scope, dashboardUsers, dashboardBrowserDataSources,
    dashboardRestDataSources) {
        'ngInject';

        _state.set(this, $state);
        _scope.set(this, $scope);
        _dashboardUsers.set(this, dashboardUsers);
        _dashboardBrowserDataSources.set(this, dashboardBrowserDataSources);
        _dashboardRestDataSources.set(this, dashboardRestDataSources);

        let chartColor = ['#FABB00', '#00A9D4', '#E32119', '#F08A00', '#89BA17', '#B1B3B4', '#00625F', '#0066B3',
            '#5C5C5C', '#7B0663', '#FF7600'];

        $scope.options = {
            chart: {
                type: 'discreteBarChart',
                height: 450,
                margin: {
                    top: 20,
                    right: 20,
                    bottom: 50,
                    left: 55
                },
                x: function(d) {
                    return d.label;
                },
                y: function(d) {
                    return d.value;
                },
                showValues: true,
                staggerLabels: true,
                color: chartColor,
                valueFormat: function(d) {
                    return d3.format(',.0f')(d);
                },
                duration: 500,
                yAxis: {
                    tickFormat: d3.format(',.0f')
                }
            }
        };

        $scope.options2 = {
            chart: {
                type: 'discreteBarChart',
                height: 450,
                margin: {
                    top: 20,
                    right: 20,
                    bottom: 50,
                    left: 55
                },
                x: function(d) {
                    return d.label;
                },
                y: function(d) {
                    return d.value;
                },
                showValues: false,
                showXAxis: false,
                color: chartColor,
                valueFormat: function(d) {
                    return d3.format(',.0f')(d);
                },
                duration: 500,
                yAxis: {
                    tickFormat: d3.format(',.0f')
                }
            }
        };

        _dashboardUsers.get(this).$promise.then((items) => {
            let data = this.convertData('Users', items.concat());
            $scope.userData = data;
        });

        _dashboardBrowserDataSources.get(this).$promise.then((items) => {
            let data = this.convertData('BrowserDataSources', items.concat());
            $scope.dataSourceData = data;
        });

        _dashboardRestDataSources.get(this).$promise.then((items) => {
            let data = this.convertData('RestDataSources', items.concat());
            $scope.dataSourceData2 = data;
        });
    }

    convertData(name, data) {
        let dataSet = [];
        let obj = {
            key: name,
            values: data
        };

        dataSet.push(obj);
        return dataSet;
    }
}
