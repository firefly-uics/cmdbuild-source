(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Localization', {

		requires: [
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localization.Localization'
		],

		constructor: function() {
			if (!Ext.isEmpty(CMDBuild) && !Ext.isEmpty(CMDBuild.configuration)) {
				// Localization configuration object
				CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION] = Ext.create('CMDBuild.model.configuration.Localization');

				var configurationObject = CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION]; // Shorthand

				CMDBuild.core.proxy.localization.Localization.getLanguages({
					scope: this,
					success: function(result, options, decodedResult) {
						// Build all languages array
						configurationObject.setLanguages(decodedResult.translations);

						// Get server language
						CMDBuild.core.proxy.localization.Localization.getCurrentLanguage({
							success: function(result, options, decodedResult) {
								configurationObject.setCurrentLanguage(decodedResult[CMDBuild.core.proxy.Constants.LANGUAGE]);
							}
						});

						/**
						 * Get enabled languages
						 *
						 * TODO: waiting for server configuration refactoring
						 */
						CMDBuild.core.proxy.Configuration.readMainConfiguration({
							success: function(response, options, decodedResult) {
								configurationObject.setEnabledLanguages(decodedResult.data.enabled_languages);
							}
						});
					}
				});
			} else {
				_error('CMDBuild or CMDBuild.configuration objects are empty', this);
			}
		}
	});

})();