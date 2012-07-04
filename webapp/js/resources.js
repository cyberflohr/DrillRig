angular.module('DrillRig.resources', [ 'ngResource' ]).factory('Config',
		function($resource) {
			return $resource('services/config/read', {}, {
				read : {
					method : 'GET',
					params : {}
				}
			});
		});