(function() {
 
'use strict';

/* Monitoring Module */
angular.module('DrillRig.monitor', [ 'DrillRig.runtime', 'DrillRig.resources' ])

	.controller('MonitoringCtrl', [ '$scope', 'runtimeServices','Config', function($scope, runtimeServices, Config) {
		$scope.runtimeServices = runtimeServices;
		$scope.config = Config.read();
		$scope.forwardStatus = {};
		//$scope.forwardStatusStyle = {
			
		
		$scope.$on('$routeChangeSuccess', function(ev) {
			$scope.runtimeServices.startMonitoring($scope);
		});
		 
		$scope.$on('$routeChangeStart', function(ev) {
			$scope.runtimeServices.stopMonitoring($scope);
		});
		
		
		//}
		$scope.stopSshClients = function() {
			$scope.runtimeServices.stopSshClients();
		}
		
		$scope.startSshClients = function() {
			$scope.runtimeServices.startSshClients($scope);
		}

	}]);

 })();
