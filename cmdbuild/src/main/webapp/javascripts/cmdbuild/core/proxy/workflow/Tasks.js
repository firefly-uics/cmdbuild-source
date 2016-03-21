(function() {

	Ext.define('CMDBuild.core.proxy.workflow.Tasks', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.workflow.tabs.taskManager.Grid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.TASK, {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.tabs.taskManager.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.tasks.workflow.getStoreByWorkflow,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TYPE, direction: 'ASC' }
				]
			});
		}
	});

})();
