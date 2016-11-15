(function () {

	/**
	 * Locale object builder
	 *
	 * FIXME: localizations required because lot of localizations doen't exists ('CMDBuild.locale.' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE))
	 */
	Ext.define('CMDBuild.core.Locale', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.locale.en',
			'CMDBuild.locale.fr',
			'CMDBuild.locale.it'
		],

		/**
		 * Setup with overrides of all data configurations (timeouts, defaultHeaders)
		 *
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			var mergedConfigObject = {};

			Ext.ns('CMDBuild.configuration');

			// Error handling
				if (!Ext.isObject(CMDBuild.configuration.runtime) || Ext.isEmpty(CMDBuild.configuration.runtime))
					return _error('constructor(): undefined CMDBuild configuration object', this);
			// END: Error handling

			// Default core localization
				Ext.Object.each(CMDBuild.locale.en.config, function (key, value, myself) {
					mergedConfigObject[key] = this.wrapDefault(value);
				}, this);

			// Override defaults with localized data
				switch (CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE)) {
					case 'fr': {
						mergedConfigObject = Ext.Object.merge(mergedConfigObject, CMDBuild.locale.fr.config);
					} break;

					case 'it': {
						mergedConfigObject = Ext.apply(mergedConfigObject, CMDBuild.locale.it.config);
					} break;
				}

			// Build locale class
			Ext.define('CMDBuild.locale.core', {
				singleton: true,

				config: mergedConfigObject
			});
		},

		/**
		 * @param {String} translation
		 *
		 * @returns {String} translation
		 *
		 * @private
		 */
		wrapDefault: function (translation) {
			return Ext.isString(translation) && !Ext.isEmpty(translation) ? '<em>' + translation + '</em>' : translation;
		}
	});

})();
