(function () {

	Ext.define('CMDBuild.proxy.management.routes.Instance', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.instance.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readWorkflow: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters);
		}
	});

})();
