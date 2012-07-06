'use strict';

// Declare app level module which depends on filters, and services
var DrillRig = angular.module('DrillRig', ['DrillRig.config', 'DrillRig.monitor' ]).

  config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {

	$routeProvider.when('/monitor', {
    	templateUrl: 'gui/partials/monitor.html', 
    	controller: 'MonitoringCtrl'
    });
	
   
    $routeProvider.otherwise({redirectTo: '/monitor'});
    $locationProvider.html5Mode(true);
  }]);
