'use strict';

/* Services */
DrillRig.controller('RuntimeManagerCtrl', [ '$scope', 'runtimeServices', function($scope, runtimeServices) {
		$scope.runtimeServices = runtimeServices;
		
		$scope.stopSshClients = function() {
			$scope.runtimeServices.stopSshClients();
		}
		
		$scope.startSshClients = function() {
			$scope.runtimeServices.startSshClients();
		}
	}
]);


//RuntimeManagerCtrl.$inject=['$scope', 'runtimeServices'];