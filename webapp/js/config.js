(function() {
		 
'use strict';

/* Config Module */
angular.module('DrillRig.config', [ ])

	/**
	 * module route configuration
	 */
  	.config(['$routeProvider', function($routeProvider) {

  		$routeProvider.when('/configuration', {
  			templateUrl: 'gui/partials/tunnel.html', 
  			controller: 'ConfigCtrl'
  		});
  	}])


	/**
	 * Config screen controller
	 */
 	.controller('ConfigCtrl', [ '$q', '$scope', 'configServices','Config', 'dialogServices', function($q, $scope, configServices, Config, dialogServices) {
		
 		$scope.dialogId = '#AddForwardDialog';
		$scope.configTunnel = {};
		$scope.infoMessages = [];
		
		$scope.addForward = function() {
			var deferred = $q.defer();
			if ($scope.AddTunnelForm.$valid) {
				configServices.addTunnel($scope).then(function(reason) {
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
		
		$scope.showAddForwardDialog = function() {
			$scope.infoMessages = [];
			$scope.configTunnel = {};
			dialogServices.showDialog("#AddForwardDialog");
		};
		
		$scope.refreshConfig = function() {
			$scope.config = Config.edit(function() {
				if ($scope.config.SshClient) {
					$scope.configTunnel.SSHClientId = $scope.config.SshClient[0];
				}
			});
		};
		
		$scope.saveConfiguration = function() {
			configServices.saveConfiguration($scope).then(function(reason) {
				$scope.infoMessages = reason; 

			}, function(reason) {
				$scope.infoMessages = reason; 
			});			
		};
		
		$scope.$on('$destroy', function(ev) {
			dialogServices.destroyDialog("#AddForwardDialog");
		});

		dialogServices.createDialog("#AddForwardDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"add tunnel" : function() {
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
		}
		
		return {
			addTunnel : function($scope) {
				
				var deferred = $q.defer();

				// detach the object data an work only with the ID   
				$scope.configTunnel.SSHClientId = $scope.configTunnel.SSHClientId ? $scope.configTunnel.SSHClientId['@id'] : '';
				
				$http({
					method : 'POST',
					url : '/services/config/tunnel/add',
					data : angular.toJson( $scope.configTunnel )
					
				}).success(function(data, status) {
					if (isServiceResultOK(data,status)) {
						localServices.logMessageBar(localServices.logLevel.INFO, 'SSH tunnel added');
						deferred.resolve(getServiceMessages(data))
					} else {
						deferred.reject(getServiceMessages(data))
					}
				}).error(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.ERROR, 'Failure addding SSH tunnel');
					deferred.reject({ data: data, status : status})
				});

				return deferred.promise;
			},
			saveConfiguration : function() {
				
				var deferred = $q.defer();

				$http({
					method : 'GET',
					url : '/services/config/save',
					
				}).success(function(data, status) {
					if (isServiceResultOK(data,status)) {
						localServices.logMessageBar(localServices.logLevel.INFO, 'New configuration loaded.');
						deferred.resolve(getServiceMessages(data))
					} else {
						deferred.reject(getServiceMessages(data))
					}
				}).error(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.ERROR, 'Failure loading configuration');
					deferred.reject({ data: data, status : status})
				});

				return deferred.promise;
			}			
		}
	} ]);

	
 })();

