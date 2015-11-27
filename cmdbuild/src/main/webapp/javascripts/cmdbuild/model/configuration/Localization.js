(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');
	Ext.require('CMDBuild.core.proxy.Localizations');

	Ext.define('CMDBuild.model.configuration.Localization', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.LANGUAGE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LANGUAGES, type: 'auto' }, // All CMDBuild languages
			{ name: CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_TAGS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS, type: 'auto' }, // CMDBuild languages with localizations
			{ name: CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS_TAGS, type: 'auto' }
		],

		/**
		 * @return {Boolean}
		 */
		hasLocalizations: function() {
			var languagesWithTrasnlations = this.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS);

			// TODO: it's needed to avoid to use "CMDBuild.Config.cmdbuild.enabled_languages", i must use that because configuration modules are initialized
			// before configuration call ends
			return Ext.isArray(languagesWithTrasnlations) && languagesWithTrasnlations.length > 0;
		},

		/**
		 * Setup languages with localizations array and all languages array (to avoid double call for same thing)
		 *
		 * @param {Mixed} enableLanguages
		 */
		setLanguagesWithLocalizations: function(enableLanguages) {
			var decodedArray = [];
			var languagesArray = [];
			var languagesTagArray = [];
			var languagesWithLocalizationsArray = [];
			var languagesWithLocalizationsTagArray = [];

			// TODO: refactor saving on server an array not a string
			if (typeof enableLanguages == 'string') {
				var splitted = enableLanguages.split(', ');

				if (Ext.isArray(splitted) && splitted.length > 0)
					decodedArray = splitted;
			} else {
				if (Ext.isArray(enableLanguages) && enableLanguages.length > 0)
					decodedArray = enableLanguages;
			}

			// Build languages objects
			CMDBuild.core.proxy.Localizations.getLanguages({
				scope: this,
				success: function(result, options, decodedResult) {
					// Build all languages array
					Ext.Array.forEach(decodedResult.translations, function(translation, i, allTranslations) {
						languagesTagArray.push(translation[CMDBuild.core.proxy.CMProxyConstants.TAG]);

						languagesArray.push(
							Ext.create('CMDBuild.model.Localizations.translation', translation)
						);
					}, this);

					this.set(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES, languagesArray);
					this.set(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_TAGS, languagesTagArray);

					// Build languages with localizations
					Ext.Array.forEach(decodedResult.translations, function(translation, i, allTranslations) {
						if (Ext.Array.contains(decodedArray, translation[CMDBuild.core.proxy.CMProxyConstants.TAG])) {
							languagesWithLocalizationsTagArray.push(translation[CMDBuild.core.proxy.CMProxyConstants.TAG]);

							languagesWithLocalizationsArray.push(
									Ext.create('CMDBuild.model.Localizations.translation', translation)
							);
						}
					}, this);

					this.set(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS, languagesWithLocalizationsArray);
					this.set(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS_TAGS, languagesWithLocalizationsTagArray);
				}
			});
		}
	});

})();