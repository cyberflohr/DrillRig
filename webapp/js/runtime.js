(function() {
 
'use strict';

/* Monitoring Module */
angular.module('DrillRig.runtime', [ 'DrillRig.logging' ])

	.service('runtimeServices', [ 'localServices', '$http', '$q', function(localServices, $http, $q ) {

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
			
			getForwardStateInfo : function() {

				var deferred = $q.defer();
				$http({
					method : 'GET',
					url : '/services/runtime/monitor/forwards'
				}).success(function(data, status) {
					
					var forwardStatus = {};
					angular.forEach(data, function(v,k) {
						forwardStatus[v.id] = v.state;
					});
					deferred.resolve(forwardStatus);
					//$scope.monitoringTimer =
					
				}).error(function(data, status) {
					deferred.reject();

				});
				return deferred.promise;
			},
		}
	}]);


 })();