'use strict';

// Declare app level module which depends on filters, and services
var DrillRig = angular.module('DrillRig', ['DrillRig.resources']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/monitor', {templateUrl: 'gui/partials/monitor.html', controller: 'RuntimeManagerCtrl'});
    $routeProvider.when('/view2', {templateUrl: 'gui/partials/partial2.html', controller: 'RuntimeManagerCtrl'});
    $routeProvider.otherwise({redirectTo: '/monitor'});
  }]);
