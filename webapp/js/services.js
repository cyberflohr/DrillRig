angular.module('DrillRigServices', [ 'ngResource' ]).factory('Config',
		function($resource) {
			return $resource('ajax/config/read', {}, {
				read : {
					method : 'GET',
					params : {}
				}
			});
		});