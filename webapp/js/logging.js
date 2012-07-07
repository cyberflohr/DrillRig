(function() {
 
'use strict';

/* Monitoring Module */
angular.module('DrillRig.logging', [ ])

	.service('localServices', [ '$rootScope', function($root) {

		return {
			logLevel : {
				ERROR : "Error",
				INFO : "Info"
			},
			logMessageBar : function(type, msg) {
				$root.globalMessage=msg;
				$("#message").fadeIn("slow");
			},
			logDialogMessageBar : function(dialog, type, msg) {
				$root.globalMessage=msg;
				$("#message").fadeIn("slow");
			}
		}
	} ]);

})();