'use strict';

/* Services */
DrillRig.service('runtimeServices', [ '$http', function($http) {

	return {
		stopSshClients : function() {

			$http({
				method : 'GET',
				url : '/ajax/runtime/stop'
			}).success(function(data, status) {
				$scope.status = status;
				$scope.data = data;
			}).error(function(data, status) {
				$scope.data = data || "Request failed";
				$scope.status = status;
			});

		},
		
		startSshClients : function() {

			$http({
				method : 'GET',
				url : '/services/runtime/start'
			}).success(function(data, status) {
				$scope.status = status;
				$scope.data = data;
			}).error(function(data, status) {
				$scope.data = data || "Request failed";
				$scope.status = status;
			});
		}
	}
} ]);