(function() {
 
'use strict';

/* Monitoring Module */
angular.module('DrillRig.monitor', [ 'DrillRig.runtime', 'DrillRig.resources' ])

	.controller('MonitoringCtrl', [ '$scope', '$timeout', 'runtimeServices','Config', function($scope, $timeout, runtimeServices, Config) {
		$scope.runtimeServices = runtimeServices;
		$scope.config = Config.read();
		$scope.forwardStatus = {};
		$scope.forwardMonitorActive=true;

		// timer function for forward state monitoring
		var monitoringTimer = function() { 
			$scope.runtimeServices.getForwardStateInfo().then(function(reason) {
				if ($scope.forwardMonitorActive) {
					$scope.forwardStatus = reason;
					$timeout(monitoringTimer, 5000, false);
				}
			});
		};		
		monitoringTimer();
		
		// stop forward monitoring on scope destroy
		$scope.$on('$destroy', function(ev) {
			$scope.forwardMonitorActive=false;
		});
		 
		// stop all SSH client sessions
		$scope.stopSshClients = function() {
			$scope.runtimeServices.stopSshClients();
		}
		
		// start all SSH client sessions
		$scope.startSshClients = function() {
			$scope.runtimeServices.startSshClients($scope);
		}

	}]);

 })();
