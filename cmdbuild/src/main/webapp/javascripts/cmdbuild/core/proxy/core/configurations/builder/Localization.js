(function () {

	Ext.define('CMDBuild.core.proxy.core.configurations.builder.Localization', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllAvailableTranslations: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.utils.readAllAvailableTranslations });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		}
	});

})();
