(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Localization', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 */
		constructor: function(parameters) {
			callback = Ext.isEmpty(parameters) ? Ext.emptyFn : parameters.callback;

			if (!Ext.isEmpty(CMDBuild) && !Ext.isEmpty(CMDBuild.configuration)) {
				CMDBuild.configuration.localization = Ext.create('CMDBuild.model.configuration.Localization'); // Localization configuration object

				CMDBuild.core.proxy.localization.Localization.getLanguages({
					scope: this,
					success: function(result, options, decodedResult) {
						// Build all languages array
						CMDBuild.configuration.localization.setLanguages(decodedResult.translations);

						// Get server language
						CMDBuild.core.proxy.Configuration.readMainConfiguration({ // TODO: waiting for server configuration refactoring
							success: function(response, options, decodedResult) {
								decodedResult = decodedResult[CMDBuild.core.constants.Proxy.DATA];

								CMDBuild.configuration.localization.set(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT, decodedResult['languageprompt']);
								CMDBuild.configuration.localization.setEnabledLanguages(decodedResult['enabled_languages']);
								CMDBuild.configuration.localization.setCurrentLanguage(decodedResult[CMDBuild.core.constants.Proxy.LANGUAGE]);
							},
							callback: callback
						});
					}
				});
			} else {
				_error('CMDBuild or CMDBuild.configuration objects are empty', this);
			}
		}
	});

})();