(function () {

	Ext.define('CMDBuild.proxy.administration.configuration.RelationGraph', {

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
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.params = Ext.isObject(parameters.params) ? parameters.params : {};
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'graph';

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.configuration.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.params = Ext.isObject(parameters.params) ? parameters.params : {};
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'graph';

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.configuration.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters, true);
		}
	});

})();
