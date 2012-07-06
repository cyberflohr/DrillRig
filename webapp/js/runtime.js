(function() {
 
'use strict';

/* Monitoring Module */
angular.module('DrillRig.runtime', [ 'DrillRig.logging' ])

	.service('runtimeServices', [ 'localServices', '$http', '$timeout', function(localServices, $http, $timeout ) {

		return {
			stopSshClients : function() {
	
				$http({
					method : 'GET',
					url : '/services/runtime/stop'
				}).success(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.INFO, 'SSH sessions stopped');
					
				}).error(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.ERROR, 'Stopping SSH sessions failed');
				});
			},
			
			startSshClients : function($scope) {
	
				$http({
					method : 'GET',
					url : '/services/runtime/start'
						
				}).success(function(data, status) {
					// $scope.onMonitoringStartet();
					localServices.logMessageBar(localServices.logLevel.INFO, 'SSH sessions started');
				}).error(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.INFO, 'SSH sessions failed to start');
				});
			},
			
			startMonitoring : function($scope) {
				if ($scope.monitoringTimer) {
					$timeout.cancel($scope.monitoringTimer);
					$scope.monitoringTimer=null;
				}

				var timerFctn = function() {
					
					$http({
						method : 'GET',
						url : '/services/runtime/monitor/forwards'
					}).success(function(data, status) {
						
						angular.forEach(data.ForwardStateInfo, function(v,k) {
							$scope.forwardStatus[v['@id']] = v['@state'];
						});
						$scope.monitoringTimer = $timeout(timerFctn, 5000, false);
						
					}).error(function(data, status) {
	
					});
					
				}
				timerFctn();
			},
			
			stopMonitoring : function($scope) {
				if ($scope.monitoringTimer) {
					$timeout.cancel($scope.monitoringTimer);
					$scope.monitoringTimer=null;
				}
			}
		}
	}]);


 })();