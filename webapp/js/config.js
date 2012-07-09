(function() {
		 
'use strict';

/* Config Module */
angular.module('DrillRig.config', [ ])

	/**
	 * module route configuration
	 */
  	.config(['$routeProvider', function($routeProvider) {

  		$routeProvider.when('/configuration', {
  			templateUrl: 'gui/partials/forward.html', 
  			controller: 'ConfigCtrl'
  		});
  	}])


	/**
	 * Config screen controller
	 */
 	.controller('ConfigCtrl', [ '$q', '$scope', 'configServices','Config', 'dialogServices', function($q, $scope, configServices, Config, dialogServices) {
		
 		$scope.dialogId = '#AddForwardDialog';
		$scope.configForward = {};
		$scope.infoMessages = [];
		
		/**
		 * Add new forward to configuration
		 */
		$scope.addForward = function() {
			var deferred = $q.defer();
			if ($scope.AddForwardForm.$valid) {

				// detach the object data an work only with the ID   
				$scope.configForward.SSHClientId = $scope.configForward.SSHClientId ? $scope.configForward.SSHClientId['@id'] : '';
				
				configServices.addForward($scope.configForward).then(function(reason) {
					$scope.infoMessages = reason;
					$scope.refreshConfig();
					deferred.resolve(reason);

				}, function(reason) {
					$scope.infoMessages = reason; 
					deferred.reject(reason);
				});
			} else {
				$scope.infoMessages = ['Bitte füllen sie alle Formularfelder korrekt aus.'];
				deferred.reject();
			}
			return deferred.promise;
		};

		/**
		 * Delete forward to configuration
		 */
		$scope.deleteForward = function(forward) {
			configServices.deleteForward(forward['@id']).then(function(reason) {
				$scope.refreshConfig();

			}, function(reason) {

			});
		};
		
		/**
		 * Edit forward to configuration
		 */
		$scope.showEditForward = function(forward) {
			
			$scope.infoMessages = [];

			var editFwd = {};
			editFwd.id = forward['@id'];
			editFwd.description = forward['@description'];
			editFwd.type = forward['@type'];
			editFwd.rHost = forward['@rHost'];
			editFwd.sHost = forward['@sHost'];
			editFwd.rPort = parseInt(forward['@rPort']);
			editFwd.sPort = parseInt(forward['@sPort']);
			editFwd.enabled = "true" == forward['@enabled'];
			
			$scope.editForward = editFwd;
			dialogServices.showDialog("#EditForwardDialog");
		};

		/**
		 * Edit forward to configuration
		 */
		$scope.changeForward = function() {
			var deferred = $q.defer();
			if ($scope.EditForwardForm.$valid) {
				configServices.changeForward( $scope.editForward).then(function(reason) {
					$scope.refreshConfig();
					deferred.resolve(reason);
	
				}, function(reason) {
					$scope.infoMessages = reason; 
					deferred.reject(reason);
				});
			} else {
				$scope.infoMessages = ['Bitte füllen sie alle Formularfelder korrekt aus.'];
				deferred.reject();
			}
			return deferred.promise;
		};
		
		/**
		 * Show "add new forward" dialog
		 */
		$scope.showAddForwardDialog = function() {
			$scope.infoMessages = [];
			$scope.configForward = {};
			if ($scope.config.SshClient) {
				$scope.configForward.SSHClientId = $scope.config.SshClient[0];
			}
			dialogServices.showDialog("#AddForwardDialog");
		};
		
		/** 
		 * reload edit configuration
		 */
		$scope.refreshConfig = function() {
			$scope.config = Config.edit();
		};
		
		/**
		 * save "edit" configuration
		 */
		$scope.saveConfiguration = function() {
			configServices.saveConfiguration($scope).then(function(reason) {
				$scope.infoMessages = reason; 

			}, function(reason) {
				$scope.infoMessages = reason; 
			});			
		};
		
		/**
		 * Scope destroy handling
		 */
		$scope.$on('$destroy', function(ev) {
			dialogServices.destroyDialog("#AddForwardDialog");
			dialogServices.destroyDialog("#EditForwardDialog");
		});

		/**
		 * create "add new forward" dialog
		 */
		dialogServices.createDialog("#AddForwardDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"add forward" : function() {
					$scope.$apply($scope.addForward).then(function() {
						dialogServices.closeDialog("#AddForwardDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#AddForwardDialog");
				}
			},
			close : function() {
			}
		});
		
		/**
		 * create "edit forward" dialog
		 */
		dialogServices.createDialog("#EditForwardDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"change" : function() {
					$scope.$apply($scope.changeForward).then(function() {
						dialogServices.closeDialog("#EditForwardDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#EditForwardDialog");
				}
			},
			close : function() {
			}
		});

		// init edit configuration
		$scope.refreshConfig();		
		
	}])
	
	/**
	 * configuration services 
	 */
	.service('configServices', [ '$q', '$rootScope',  'localServices', '$http', function($q, $root, localServices, $http ) {
		
		var isServiceResultOK = function(data, status) {
			return status == 200 && data && data.ServiceStatus.code == 'OK';
		};
		var getServiceMessages = function(data) {
			if (data && data.ServiceStatus) {
				if (typeof data.ServiceStatus.msg == 'string') {
					return [data.ServiceStatus.msg];
				} else {
					return data.ServiceStatus.msg;
				}
			} else {
				return [];
			}
		};
		var httpService = function(data) {
			
			var deferred = $q.defer();

			$http(data).success(function(data, status) {
				if (isServiceResultOK(data,status)) {
					deferred.resolve(getServiceMessages(data))
				} else {
					deferred.reject(getServiceMessages(data))
				}
			}).error(function(data, status) {
//				localServices.logMessageBar(localServices.logLevel.ERROR, 'Failure addding SSH tunnel');
				deferred.reject({ data: data, status : status})
			});

			return deferred.promise;
		};
		
		return {
			addForward : function(configForward) {
				
				return httpService({
					method : 'POST',
					url : '/services/config/forward/add',
					data : angular.toJson( configForward )
					
				});
			},

			deleteForward : function(forwardId) {
				
				return httpService({
					method : 'DELETE',
					url : '/services/config/forward/delete/' + forwardId
				});
			},
			
			changeForward : function(data) {
				
				return httpService({
					method : 'POST',
					url : '/services/config/forward/change/' + data.id,
					data : angular.toJson( data )
				});
			},

			saveConfiguration : function() {
				
				return httpService({
					method : 'GET',
					url : '/services/config/save',
					
				});
			}			
		}
	} ]);

	
 })();

