'use strict';

/* Services */
DrillRig.controller('RuntimeManagerCtrl', [ '$scope', 'runtimeServices','Config', function($scope, runtimeServices, Config) {
		$scope.runtimeServices = runtimeServices;
		$scope.config = Config.read();
		$scope.forwardStatus = {};
		//$scope.forwardStatusStyle = {
				
		//}
		$scope.stopSshClients = function() {
			$scope.runtimeServices.stopSshClients();
		}
		
		$scope.startSshClients = function() {
			$scope.runtimeServices.startSshClients($scope);
		}
		
		$scope.startMonitoring = function() {
			$scope.runtimeServices.startMonitoring($scope);
		}
		
		$scope.onMonitoringStartet = function() {
			$scope.startMonitoring();
		}
		
		$scope.startMonitoring();
	}
]);


//RuntimeManagerCtrl.$inject=['$scope', 'runtimeServices'];