'use strict';

// Declare app level module which depends on filters, and services
var DrillRig = angular.module('DrillRig',
		[ 'DrillRig.config', 'DrillRig.monitor', 'DrillRig.navigation' ])


/**
 * dialog services 
 */
.service('dialogServices', [ '$q', function($q) {

	return {
		showDialog : function(dialogId) {
			$(dialogId).dialog("open");
		},

		createDialog : function(dialogId, options) {

			var dialog = $(dialogId);
			dialog.dialog(options);
		},

		destroyDialog : function(dialogId) {
			$(dialogId).dialog("destroy").remove();
		},

		closeDialog : function(dialogId) {
			$(dialogId).dialog("close");
		}
	}
} ])

	.controller('GlobalCtrl', [ '$scope', '$timeout', 'runtimeServices','Config', function($scope, $timeout, runtimeServices, Config) {
		
		$scope.getSourceHost = function(config, forward) {
		
			var display= forward.sHost;
			var host = (forward.type == "L" ? config.host : forward.connection.name);
			switch (forward.sHost) {
			case 'localhost':
				display = host + ' (local only)';
				break;
			case '':
			case null:
				display = host;
				break;
			}
			return display;
		}

		$scope.getRemoteHost = function(config, forward) {
			
			var display= forward.rHost;
			var host = (forward.type == "R" ? config.host : forward.connection.name);
			switch (forward.rHost) {
			case 'localhost':
				display = host + ' (local only)';
				break;
			case '':
			case null:
				display = host;
				break;
			}
			return display;
		}
	}]);
