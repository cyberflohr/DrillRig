(function() {
		 
'use strict';

/* Config Module */
angular.module('DrillRig.config', [ ])

  	.config(['$routeProvider', function($routeProvider) {

  		$routeProvider.when('/configuration', {
  			templateUrl: 'gui/partials/tunnel.html', 
  			controller: 'ConfigCtrl'
  		});
  	}])


 	.controller('ConfigCtrl', [ '$scope', 'configServices','Config', function($scope, configServices, Config) {
		
		$scope.configTunnel = {};
		
		
		$scope.addTunnel = function() {
			configServices.addTunnel($scope);
		};
		
		$scope.showAddTunnelDialog = function() {
			showAddTunnelDialog();
		};
		
		$scope.refreshConfig = function() {
			$scope.config = Config.read(function() {
				if ($scope.config.SshClient) {
					$scope.configTunnel.SSHClientId = $scope.config.SshClient[0];
				}
			});
		};
		
		createDialog($scope);
		$scope.refreshConfig();		
		
	}])
	
	.service('configServices', [ '$rootScope',  'localServices', '$http', function($root, localServices, $http ) {
		return {
			addTunnel : function($scope) {
				
				// detach the object data an work only with the ID   
				$scope.configTunnel.SSHClientId = $scope.configTunnel.SSHClientId['@id'];
				
				$http({
					method : 'POST',
					url : '/services/config/tunnel/add',
					data : angular.toJson( $scope.configTunnel )
				}).success(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.INFO, 'SSH tunnel added');
					$scope.refreshConfig();		
					
				}).error(function(data, status) {
					localServices.logMessageBar(localServices.logLevel.ERROR, 'Failure addding SSH tunnel');
				});
			}
		}
	} ]);

	// general purpose functions
	function showAddTunnelDialog() {
		$( "#dialog-form" ).dialog( "open" );
	}
	
	function createDialog($scope) {
		
		$("#dialog-form").dialog({
			autoOpen : false,
			height : 300,
			width : 350,
			modal : true,
			buttons : {
				"add tunnel" : function() {
					$scope.addTunnel($scope);
					$(this).dialog("close");
				},
				cancel : function() {
					$(this).dialog("close");
				}
			},
			close : function() {
			}
		});
	}
	
 })();

