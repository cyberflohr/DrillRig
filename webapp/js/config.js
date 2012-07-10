(function() {
		 
'use strict';

/* Config Module */
angular.module('DrillRig.config', [ ])

	/**
	 * Config forward controller
	 */
 	.controller('ConfigForwardCtrl', [ '$q', '$scope', 'configServices','Config', 'dialogServices','localServices', function($q, $scope, configServices, Config, dialogServices,localServices) {
		
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
				$scope.configForward.SSHClientId = $scope.configForward.SSHClientId ? $scope.configForward.SSHClientId['id'] : '';
				
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
		 * Add new session to configuration
		 */
		$scope.addSession = function() {
			var deferred = $q.defer();
			if ($scope.AddSessionForm.$valid) {

				// detach the object data an work only with the ID   
				$scope.configSession.machineAccount = $scope.configSession.machineAccount ? $scope.configSession.machineAccount['id'] : '';
				
				configServices.addSession($scope.configSession).then(function(reason) {
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
			configServices.deleteForward(forward['id']).then(function(reason) {
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
			editFwd.id = forward['id'];
			editFwd.description = forward['description'];
			editFwd.type = forward['type'];
			editFwd.rHost = forward['rHost'];
			editFwd.sHost = forward['sHost'];
			editFwd.rPort = forward['rPort'];
			editFwd.sPort = forward['sPort'];
			editFwd.enabled = forward['enabled'];
			
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
		 * Show "add new session" dialog
		 */
		$scope.showAddSessionDialog = function() {
			$scope.infoMessages = [];
			$scope.configSession = {};
			if ($scope.config.MachineAccount) {
				$scope.configForward.machineAccount = $scope.config.MachineAccount[0];
			}
			dialogServices.showDialog("#AddSessionDialog");
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
				localServices.logMessageBar(localServices.logLevel.INFO, 'New configuration activated.');

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
			dialogServices.destroyDialog("#AddSessionDialog");
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
		 * create "add new session" dialog
		 */
		dialogServices.createDialog("#AddSessionDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"add session" : function() {
					$scope.$apply($scope.addSession).then(function() {
						dialogServices.closeDialog("#AddSessionDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#AddSessionDialog");
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
	 * Config forward controller
	 */
 	.controller('ConfigMachineCtrl', [ '$q', '$scope', 'configServices','Config', 'dialogServices','localServices', function($q, $scope, configServices, Config, dialogServices,localServices) {
 		
		/**
		 * Add new machine to configuration
		 */
		$scope.addMachine = function() {
			var deferred = $q.defer();
			if ($scope.AddMachineForm.$valid) {

				configServices.addMachine($scope.configMachine).then(function(reason) {
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
		 * Delete machine to configuration
		 */
		$scope.deleteMachine = function(machine) {
			configServices.deleteMachine(machine['id']).then(function(reason) {
				$scope.refreshConfig();

			}, function(reason) {
				localServices.logMessageBar(localServices.logLevel.ERROR, reason);

			});
		};
		
		/**
		 * Edit machine configuration
		 */
		$scope.showEditMachine = function(machine) {
			
			$scope.infoMessages = [];

			var editMachine = {};
			editMachine.id = machine['id'];
			editMachine.name = machine['name'];
			editMachine.host = machine['host'];
			editMachine.port = machine['port'];
			editMachine.user = machine['user'];
			editMachine.password = machine['password'];
			
			$scope.editMachine = editMachine;
			dialogServices.showDialog("#EditMachineDialog");
		};

		/**
		 * Edit machine configuration
		 */
		$scope.changeMachine = function() {
			var deferred = $q.defer();
			if ($scope.EditMachineForm.$valid) {
				configServices.changeMachine( $scope.editMachine).then(function(reason) {
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
		 * Show "add new machine" dialog
		 */
		$scope.showAddMachineDialog = function() {
			$scope.infoMessages = [];
			$scope.configMachine = {};

			dialogServices.showDialog("#AddMachineDialog");
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
				localServices.logMessageBar(localServices.logLevel.INFO, 'New configuration activated.');

			}, function(reason) {
				$scope.infoMessages = reason; 
			});			
		};
		
		/**
		 * Scope destroy handling
		 */
		$scope.$on('$destroy', function(ev) {
			dialogServices.destroyDialog("#AddMachineDialog");
			dialogServices.destroyDialog("#EditMachineDialog");
		});

		/**
		 * create "add new machine" dialog
		 */
		dialogServices.createDialog("#AddMachineDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"add machine" : function() {
					$scope.$apply($scope.addMachine).then(function() {
						dialogServices.closeDialog("#AddMachineDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#AddMachineDialog");
				}
			},
			close : function() {
			}
		});
		
		/**
		 * create "edit machine" dialog
		 */
		dialogServices.createDialog("#EditMachineDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"change" : function() {
					$scope.$apply($scope.changeMachine).then(function() {
						dialogServices.closeDialog("#EditMachineDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#EditMachineDialog");
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
				var id = data.id;
				delete data.id;
				return httpService({
					method : 'POST',
					url : '/services/config/forward/change/' + id,
					data : angular.toJson( data )
				});
			},

			addSession : function(sessionConfig) {
				
				return httpService({
					method : 'POST',
					url : '/services/config/session/add',
					data : angular.toJson( sessionConfig )
					
				});
			},

			addMachine : function(configForward) {
				
				return httpService({
					method : 'POST',
					url : '/services/config/machine/add',
					data : angular.toJson( configForward )
					
				});
			},

			deleteMachine : function(forwardId) {
				
				return httpService({
					method : 'DELETE',
					url : '/services/config/machine/delete/' + forwardId
				});
			},
			
			changeMachine : function(data) {
				var id = data.id;
				delete data.id;
				return httpService({
					method : 'POST',
					url : '/services/config/machine/change/' + id,
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

