(function() {

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
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.LOCALIZATION] = Ext.create('CMDBuild.model.configuration.Localization'); // Localization configuration object

			CMDBuild.core.proxy.localization.Localization.getLanguages({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.TRANSLATIONS];

					// Build all languages array
					CMDBuild.configuration[CMDBuild.core.constants.Proxy.LOCALIZATION].setLanguages(decodedResponse);

					// Get server language
					CMDBuild.core.proxy.configuration.GeneralOptions.read({ // TODO: waiting for server configuration refactoring
						loadMask: false,
						scope: this,
						success: function(response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

							CMDBuild.configuration[CMDBuild.core.constants.Proxy.LOCALIZATION].set(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT, decodedResponse['languageprompt']);
							CMDBuild.configuration[CMDBuild.core.constants.Proxy.LOCALIZATION].setEnabledLanguages(decodedResponse['enabled_languages']);
							CMDBuild.configuration[CMDBuild.core.constants.Proxy.LOCALIZATION].setCurrentLanguage(decodedResponse[CMDBuild.core.constants.Proxy.LANGUAGE]);
						},
						callback: this.callback
					});
				}
			});
		}
	});

})();