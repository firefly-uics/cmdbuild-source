(function() {

	/**
	 * Extension of classes proxy class because this would be a right way (different end-point for different resources) .. waiting for server refactor
	 */
	Ext.define('CMDBuild.core.proxy.common.tabs.history.Processes', {

		requires: [
			'CMDBuild.core.proxy.common.tabs.history.Classes',
			'CMDBuild.model.workflow.tabs.history.CardRecord'
		],

		singleton: true,

		/**
		 * @property {Object} parameters
		 */
		get: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.get(parameters);
		},

		/**
		 * @property {Object} parameters
		 */
		getHistoric: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.getHistoric(parameters);
		},

		/**
		 * @property {Object} parameters
		 */
		getRelations: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.getRelations(parameters);
		},

		/**
		 * @property {Object} parameters
		 */
		getRelationHistoric: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.getRelationHistoric(parameters);
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.tabs.history.CardRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.history.processes.getProcessHistory,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [ // Setup sorters, also if server returns ordered collection
					{ property: CMDBuild.core.constants.Proxy.BEGIN_DATE, direction: 'DESC' }
				]
			});
		}
	});

})();