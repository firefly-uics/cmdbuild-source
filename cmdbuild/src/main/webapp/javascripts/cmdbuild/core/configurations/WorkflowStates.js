(function () {

	Ext.define('CMDBuild.core.configurations.WorkflowStates', {

		singleton: true,

		config: {
			aborted: 'closed.aborted',
			all: 'all',
			completed: 'closed.completed',
			open: 'open.running',
			suspended: 'open.not_running.suspended'
		},

		/**
		 * @param {Object} config
		 *
		 * @returns {Void}
		 */
		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();
