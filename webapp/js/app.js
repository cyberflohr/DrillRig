'use strict';

// Declare app level module which depends on filters, and services
var DrillRig = angular.module('DrillRig', ['DrillRig.resources']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/view1', {templateUrl: 'partials/partial1.html', controller: 'runtimeManagerCtrl'});
    $routeProvider.when('/view2', {templateUrl: 'partials/partial2.html', controller: 'RuntimeManagerCtrl'});
    $routeProvider.otherwise({redirectTo: '/view1'});
  }]);
