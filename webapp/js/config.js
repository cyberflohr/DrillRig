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
		
		var findElement = function(id, arr) {
			var el=null;
			$(arr).each(function(k,v) {
				if (v.id === id) {
					el=v;
				}
			});
			return el;
		};

		/**
		 * Add new forward to configuration
		 */
		$scope.addForward = function() {
			var deferred = $q.defer();
			if ($scope.AddForwardForm.$valid) {

				// detach the object data an work only with the ID   
				var sessionId = $scope.configForward.session;
				delete $scope.configForward.session; 
				
				// detach the object data an work only with the ID
				var connection = $scope.configForward.connection;
				$scope.configForward.connection = $scope.configForward.connection ? $scope.configForward.connection['id'] : '';
				
				
				configServices.addForward($scope.configForward,  sessionId).then(function(reason) {
					$scope.infoMessages = reason;
					$scope.refreshConfig();
					deferred.resolve(reason);

				}, function(reason) {
					$scope.configForward.session = sessionId;
					$scope.configForward.connection = connection;
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
		 * Delete session from configuration
		 */
		$scope.deleteSession = function(session) {
			configServices.deleteSession(session['id']).then(function(reason) {
				$scope.refreshConfig();

			}, function(reason) {

			});
		};
		
		/**
		 * Edit forward to configuration
		 */
		$scope.updateForward = function() {
			var deferred = $q.defer();
			if ($scope.EditForwardForm.$valid) {
				var connection =  $scope.editForward.connection;
				$scope.editForward.connection = connection.id;
				if ($scope.editForwardFilter.length > 0) {
					$scope.editForward.filter = { 
						block:$scope.IpMaskBlacklist, 
						mask : $scope.editForwardFilter,
						enabled:true
					};	
				} else {
					$scope.editForward.filter=null; 
				}
				
				configServices.updateForward( $scope.editForward).then(function(reason) {
					$scope.refreshConfig();
					deferred.resolve(reason);
	
				}, function(reason) {
					$scope.editForward.connection = connection;
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
		 * Edit session configuration
		 */
		$scope.updateSession = function() {
			var deferred = $q.defer();
			if ($scope.EditSessionForm.$valid) {
				
				configServices.updateSession( $scope.configSession).then(function(reason) {
					$scope.refreshConfig();
					deferred.resolve(reason);
	
				}, function(reason) {
					$scope.infoMessages = reason; 
					$scope.configSession.connection = connection;
					
					deferred.reject(reason);
				});
			} else {
				$scope.infoMessages = ['Bitte füllen sie alle Formularfelder korrekt aus.'];
				deferred.reject();
			}
			return deferred.promise;
		};

		/**
		 * Edit forward to configuration
		 */
		$scope.showEditForwardDialog = function(forward) {
			
			$scope.infoMessages = [];

			var editFwd = {};
			editFwd.id = forward['id'];
			editFwd.connection = findElement(forward.connection.id, $scope.config.Connection);

			editFwd.description = forward['description'];
			editFwd.type = forward['type'];
			editFwd.rHost = forward['rHost'];
			editFwd.sHost = forward['sHost'];
			editFwd.rPort = forward['rPort'];
			editFwd.sPort = forward['sPort'];
			editFwd.enabled = forward['enabled'];
			editFwd.filter = forward.filter;
			$scope.editForwardFilter = forward.filter ? [].concat(forward.filter.mask) : [];	
			$scope.IpMaskBlacklist =  forward.filter ? forward.filter.block :false;
			
			$scope.editForward = editFwd;
			dialogServices.showDialog("#EditForwardDialog");
		};

		/**
		 * Show "add new forward" dialog
		 */
		$scope.showAddForwardDialog = function() {
			$scope.infoMessages = [];
			$scope.configForward = {};
			if ($scope.config.SshSession) {
				$scope.configForward.session = $scope.config.SshSession[0];
			}
			dialogServices.showDialog("#AddForwardDialog");
		};
		
		/**
		 * Show "add new session" dialog
		 */
		$scope.showAddSessionDialog = function() {
			$scope.infoMessages = [];
			$scope.configSession = {};

			dialogServices.showDialog("#AddSessionDialog");
		};
		
		/**
		 * Show "edit session" dialog
		 */
		$scope.showEditSessionDialog = function(session) {
			$scope.infoMessages = [];
			$scope.configSession = {};
			
			$scope.configSession.id = session.id;
			$scope.configSession.name = session.name;
			$scope.configSession.description = session.description;
			$scope.configSession.enabled = session.enabled;

			dialogServices.showDialog("#EditSessionDialog");
		};

		$scope.addIpMask = function(IpMask) {
			if (IpMask != "") {
				$scope.editForwardFilter.push(IpMask);
				$scope.IpMask='';
			}
		};
		
		$scope.removeIpMask = function(IpMask) {
			for (var s in $scope.editForwardFilter) {
				if ($scope.editForwardFilter[s] == $scope.editForward.filter) {
					$scope.editForwardFilter.splice(s, 1);
					break;
				}
			}
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
			dialogServices.destroyDialog("#EditSessionDialog");
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
		 * create "edit session" dialog
		 */
		dialogServices.createDialog("#EditSessionDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"update session" : function() {
					$scope.$apply($scope.updateSession).then(function() {
						dialogServices.closeDialog("#EditSessionDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#EditSessionDialog");
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
			width : 490,
			modal : true,
			buttons : {
				"update forward" : function() {
					$scope.$apply($scope.updateForward).then(function() {
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
 	.controller('ConfigConnectionCtrl', [ '$q', '$scope', 'configServices','Config', 'dialogServices','localServices', function($q, $scope, configServices, Config, dialogServices,localServices) {
 		
		/**
		 * Add new connection to configuration
		 */
		$scope.addConnection = function() {
			var deferred = $q.defer();
			if ($scope.AddConnectionForm.$valid) {

				configServices.addConnection($scope.configConnection).then(function(reason) {
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
		 * Delete connection to configuration
		 */
		$scope.deleteConnection = function(connection) {
			configServices.deleteConnection(connection['id']).then(function(reason) {
				$scope.refreshConfig();

			}, function(reason) {
				localServices.logMessageBar(localServices.logLevel.ERROR, reason);

			});
		};
		
		/**
		 * Edit connection configuration
		 */
		$scope.showEditConnection = function(connection) {
			
			$scope.infoMessages = [];

			var editConnection = {};
			editConnection.id = connection['id'];
			editConnection.name = connection['name'];
			editConnection.host = connection['host'];
			editConnection.port = connection['port'];
			editConnection.user = connection['user'];
			editConnection.password = connection['password'];
			
			$scope.editConnection = editConnection;
			dialogServices.showDialog("#EditConnectionDialog");
		};

		/**
		 * Edit connection configuration
		 */
		$scope.updateConnection = function() {
			var deferred = $q.defer();
			if ($scope.EditConnectionForm.$valid) {
				configServices.updateConnection( $scope.editConnection).then(function(reason) {
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
		 * Show "add new connection" dialog
		 */
		$scope.showAddConnectionDialog = function() {
			$scope.infoMessages = [];
			$scope.configConnection = {};

			dialogServices.showDialog("#AddConnectionDialog");
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
			dialogServices.destroyDialog("#AddConnectionDialog");
			dialogServices.destroyDialog("#EditConnectionDialog");
		});

		/**
		 * create "add new connection" dialog
		 */
		dialogServices.createDialog("#AddConnectionDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"add connection" : function() {
					$scope.$apply($scope.addConnection).then(function() {
						dialogServices.closeDialog("#AddConnectionDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#AddConnectionDialog");
				}
			},
			close : function() {
			}
		});
		
		/**
		 * create "edit connection" dialog
		 */
		dialogServices.createDialog("#EditConnectionDialog", {
			autoOpen : false,
			width : 420,
			modal : true,
			buttons : {
				"change" : function() {
					$scope.$apply($scope.updateConnection).then(function() {
						dialogServices.closeDialog("#EditConnectionDialog");
					});		
				},
				cancel : function() {
					dialogServices.closeDialog("#EditConnectionDialog");
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
					url : '/services/config/forward/add/'+sessionId,
					data : angular.toJson( configForward )
					
				});
			},

			deleteForward : function(forwardId) {
				
				return httpService({
					method : 'DELETE',
					url : '/services/config/forward/delete/' + forwardId
				});
			},
			
			deleteSession : function(sessionId) {
				
				return httpService({
					method : 'DELETE',
					url : '/services/config/session/delete/' + sessionId
				});
			},

			updateForward : function(data) {
				var id = data.id;
				delete data.id;
				return httpService({
					method : 'POST',
					url : '/services/config/forward/update/' + id,
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

			addConnection : function(configForward) {
				
				return httpService({
					method : 'POST',
					url : '/services/config/connection/add',
					data : angular.toJson( configForward )
					
				});
			},

			deleteConnection : function(forwardId) {
				
				return httpService({
					method : 'DELETE',
					url : '/services/config/connection/delete/' + forwardId
				});
			},
			
			updateConnection : function(data) {
				var id = data.id;
				delete data.id;
				return httpService({
					method : 'POST',
					url : '/services/config/connection/update/' + id,
					data : angular.toJson( data )
				});
			},

			updateSession : function(data) {
				return httpService({
					method : 'POST',
					url : '/services/config/session/update/' + data.id,
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

