(function () {

	Ext.define('CMDBuild.core.configurationBuilders.Localization', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.GeneralOptions',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * Enable or disable server calls (set as false within contexts where server calls aren't enabled)
		 *
		 * @cfg {Boolean}
		 */
		enableServerCalls: true,

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {Boolean} configuration.enableServerCalls
		 *
		 * @override
		 */
		constructor: function (configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.localization = Ext.create('CMDBuild.model.configuration.Localization'); // Localization configuration object

			if (this.enableServerCalls)
				CMDBuild.core.proxy.localization.Localization.getLanguages({
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.TRANSLATIONS];

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							CMDBuild.configuration.localization.set(CMDBuild.core.constants.Proxy.LANGUAGES, decodedResponse); // Build all languages array

							// Get server language
							CMDBuild.core.proxy.configuration.GeneralOptions.read({ // TODO: waiting for refactor (server configuration refactoring)
								loadMask: false,
								scope: this,
								success: function (response, options, decodedResponse) {
									decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

									if (!Ext.isEmpty(decodedResponse) && Ext.isObject(decodedResponse)) {
										CMDBuild.configuration.localization.set(CMDBuild.core.constants.Proxy.ENABLED_LANGUAGES, decodedResponse['enabled_languages']);
										CMDBuild.configuration.localization.set(CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE, decodedResponse[CMDBuild.core.constants.Proxy.LANGUAGE]);
										CMDBuild.configuration.localization.set(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT, decodedResponse['languageprompt']);
									}
								},
								callback: this.callback
							});
						}
					}
				});
		}
	});

})();
