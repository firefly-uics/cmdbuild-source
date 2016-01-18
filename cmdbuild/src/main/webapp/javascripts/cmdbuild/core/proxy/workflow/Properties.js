(function() {

	Ext.define('CMDBuild.core.proxy.workflow.Properties', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.workflow.tabs.properties.Parent'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreSuperProcesses: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.WORKFLOW, {
				autoLoad: true,
				model: 'CMDBuild.model.workflow.tabs.properties.Parent',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters classes and all non superclass processes
						return (
							record.get(CMDBuild.core.constants.Proxy.TYPE) == 'processclass'
							&& record.get('superclass')
						);
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		}
	});

})();