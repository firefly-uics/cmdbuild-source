(function () {

	Ext.define('CMDBuild.controller.management.workflow.Utils', {

		requires: ['CMDBuild.core.configurations.WorkflowStates'],

		singleton: true,

		/**
		 * @param {String} status
		 *
		 * @returns {String}
		 */
		translateStatusFromCapitalizedMode: function (status) {
			switch (status) {
				case CMDBuild.core.configurations.WorkflowStates.getOpenCapitalized():
					return CMDBuild.core.configurations.WorkflowStates.getOpen();

				case CMDBuild.core.configurations.WorkflowStates.getSuspendedCapitalized():
					return CMDBuild.core.configurations.WorkflowStates.getSuspended();

				case CMDBuild.core.configurations.WorkflowStates.getCompletedCapitalized():
					return CMDBuild.core.configurations.WorkflowStates.getCompleted();

				case CMDBuild.core.configurations.WorkflowStates.getAbortedCapitalized():
					return CMDBuild.core.configurations.WorkflowStates.getAborted();

				default: {
					_error('translateStatusFromCapitalizedMode(): unmanaged status parameter', this, status);

					return null;
				}
			}
		}
	});

})();
