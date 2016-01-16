(function() {

	Ext.define('CMDBuild.core.proxy.workflow.Workflow', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters, true);
		}
	});

})();