
/* Config Module */
angular.module('DrillRig.navigation', [ ])

	/**
	 * module route configuration
	 */
  	.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {

  		$routeProvider.when('/configuration', {
  			templateUrl: 'gui/partials/forward.html', 
  			controller: 'ConfigCtrl', 
  			resolve : {
  				navi : function() {
  					//$scope.firstLevel=2;
  					//$scope.secondLevel=1;
  				}
  			}
  		});

		$routeProvider.when('/monitor', {
			templateUrl : 'gui/partials/monitor.html',
			controller : 'MonitoringCtrl'
		});

		$routeProvider.otherwise({
			redirectTo : '/monitor'
		});

		$locationProvider.html5Mode(true);
  		
  	}])
  	
  	.controller('NavigationCtrl', [ function() {
  		
  	}]);