(function () {

	Ext.define('CMDBuild.core.proxy.configuration.Workflow', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.params = Ext.isEmpty(parameters.params) ? {} : parameters.params;
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'workflow';

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.configuration.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.params = Ext.isEmpty(parameters.params) ? {} : parameters.params;
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'workflow';

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.configuration.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters, true);
		}
	});

})();
