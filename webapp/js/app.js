'use strict';

// Declare app level module which depends on filters, and services
var DrillRig = angular.module('DrillRig', ['DrillRig.resources']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/monitor', {templateUrl: 'gui/partials/monitor.html', controller: 'RuntimeManagerCtrl'});
    $routeProvider.when('/configuration', {templateUrl: 'gui/partials/tunnel.html', controller: 'RuntimeManagerCtrl'});
    $routeProvider.otherwise({redirectTo: '/monitor'});
  }]);
