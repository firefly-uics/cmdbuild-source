(function() {

	Ext.define('CMDBuild.model.configuration.Localization', {
		extend: 'Ext.data.Model',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localization.Localization'
		],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.LANGUAGE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.LANGUAGES, type: 'auto' }, // All CMDBuild languages
			{ name: CMDBuild.core.proxy.Constants.ENABLED_LANGUAGES, type: 'auto' } // CMDBuild enabled languages
		],

		/**
		 * @return {Object}
		 */
		getAllLanguages: function() {
			return this.get(CMDBuild.core.proxy.Constants.LANGUAGES) || {};
		},

		/**
		 * @return {Object}
		 */
		getEnabledLanguages: function() {
			var enabledLanguages = {};

			Ext.Array.forEach(this.get(CMDBuild.core.proxy.Constants.ENABLED_LANGUAGES), function(languageTag, i, allLanguageTag) {
				enabledLanguages[languageTag] = this.getLanguageObject(languageTag);
			},this);

			return enabledLanguages;
		},

		/**
		 * @param {String} languageTag
		 *
		 * @return {CMDBuild.model.localization.Localization} or null
		 */
		getLanguageObject: function(languageTag) {
			if (this.isManagedLanguage(languageTag))
				return this.get(CMDBuild.core.proxy.Constants.LANGUAGES)[languageTag];

			return null;
		},

		/**
		 * @return {Boolean}
		 */
		hasEnabledLanguages: function() {
			var enabledLanguages = this.get(CMDBuild.core.proxy.Constants.ENABLED_LANGUAGES);

			return Ext.isArray(enabledLanguages) && enabledLanguages.length > 0;
		},

		/**
		 * @param {String} languageTag
		 *
		 * @return {Boolean}
		 */
		isManagedLanguage: function(languageTag) {
			return this.get(CMDBuild.core.proxy.Constants.LANGUAGES).hasOwnProperty(languageTag);
		},

		/**
		 * @param {String} languageTag
		 */
		setCurrentLanguage: function(languageTag) {
			if (!Ext.isEmpty(languageTag) && this.isManagedLanguage(languageTag)) {
				this.set(CMDBuild.core.proxy.Constants.LANGUAGE, languageTag);
			} else {
				_error('empty language tag', this);
			}
		},

		/**
		 * Setup enabled languages
		 *
		 * @param {Mixed} enableLanguages
		 */
		setEnabledLanguages: function(enableLanguages) {
			var decodedArray = [];
			var enabledLanguages = [];

			// TODO: refactor saving on server an array not a string
			if (typeof enableLanguages == 'string') {
				var splitted = enableLanguages.split(', ');

				if (Ext.isArray(splitted) && splitted.length > 0)
					decodedArray = splitted;
			} else {
				if (Ext.isArray(enableLanguages) && enableLanguages.length > 0)
					decodedArray = enableLanguages;
			}

			// Build languages with localizations
			Ext.Array.forEach(decodedArray, function(languageTag, i, allLanguageTag) {
				if (this.isManagedLanguage(languageTag))
					enabledLanguages.push(languageTag);
			}, this);

			this.set(CMDBuild.core.proxy.Constants.ENABLED_LANGUAGES, enabledLanguages);
		},

		/**
		 * @param {Array} languages
		 */
		setLanguages: function(languages) {
			var languagesObjectsArray = {};

			if (Ext.isArray(languages)) {
				Ext.Array.forEach(languages, function(language, i, allLanguages) {
					languagesObjectsArray[language[CMDBuild.core.proxy.Constants.TAG]] = Ext.create('CMDBuild.model.localization.Localization', language);
				}, this);

				this.set(CMDBuild.core.proxy.Constants.LANGUAGES, languagesObjectsArray);
			} else {
				_error('wrong languages array format', this);
			}
		}
	});

})();