(function() {

	/**
	 * Extension of classes proxy class because this would be a right way (different end-point for different resources) .. waiting for server refactor
	 */
	Ext.define('CMDBuild.core.proxy.common.tabs.history.Processes', {

		requires: ['CMDBuild.core.proxy.common.tabs.history.Classes'],

		singleton: true,

		/**
		 * @property {Object} params
		 */
		get: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.get(parameters);
		},

		/**
		 * @property {Object} params
		 */
		getHistoric: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.getHistoric(parameters);
		},

		/**
		 * @property {Object} params
		 */
		getRelations: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.getRelations(parameters);
		},

		/**
		 * @property {Object} params
		 */
		getRelationHistoric: function(parameters) {
			CMDBuild.core.proxy.common.tabs.history.Classes.getRelationHistoric(parameters);
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return CMDBuild.core.proxy.common.tabs.history.Classes.getStore();
		}
	});

})();