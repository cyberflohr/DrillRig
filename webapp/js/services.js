'use strict';

/* Services */
DrillRig.service('runtimeServices', [ 'localServices', '$http', '$timeout', function(localServices, $http, $timeout ) {

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
				//$scope.onMonitoringStartet();
				localServices.logMessageBar(localServices.logLevel.INFO, 'SSH sessions started');
			}).error(function(data, status) {
				localServices.logMessageBar(localServices.logLevel.INFO, 'SSH sessions failed to start');
			});
		},
		
		startMonitoring : function($scope) {
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
		}
	}
} ]);

DrillRig.service('localServices', [ '$rootScope', function($root) {
	return {
		logLevel : {
			ERROR : "Error",
			INFO : "Info"
		},
		logMessageBar : function(type, msg) {
			$root.globalMessage=msg;
			$("#message").fadeIn("slow");
		}
	}
} ]);

