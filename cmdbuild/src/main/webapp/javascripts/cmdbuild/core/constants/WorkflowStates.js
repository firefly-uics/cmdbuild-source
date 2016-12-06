(function () {

	Ext.define('CMDBuild.core.constants.WorkflowStates', {

		singleton: true,

		config: {
			aborted: 'closed.aborted',
			abortedCapitalized: 'ABORTED',
			all: 'all',
			completed: 'closed.completed',
			completedCapitalized: 'COMPLETED',
			open: 'open.running',
			openCapitalized: 'OPEN',
			suspended: 'open.not_running.suspended',
			suspendedCapitalized: 'SUSPENDED'
		}
	});

})();
