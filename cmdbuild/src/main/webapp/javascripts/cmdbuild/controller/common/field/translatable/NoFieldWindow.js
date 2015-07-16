(function() {

	/**
	 * Customization of CMDBuild.controller.common.field.translatable.Window, mainly used in menu translations tree
	 */
	Ext.define('CMDBuild.controller.common.field.translatable.NoFieldWindow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localizations.Localizations'
		],

		/**
		 * Buffer object where save translatable values
		 *
		 * @cfg {Object}
		 */
		buffer: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTranslatableWindowAbortButtonClick',
			'onTranslatableWindowBeforeShow',
			'onTranslatableWindowConfirmButtonClick',
		],

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		/**
		 * Field translation properties.
		 * NOTE: owner and identifier could be objects (key, form) to use with getData() to get data from server
		 *
		 * @cfg {Object}
		 *
		 * Ex. {
		 * 		{String} type: entity type identifier (class, attributeclass, domain, attributedomain, filter, instancename, lookupvalue, menuitem, report, view, classwidget)
		 * 		{Object or String} owner: translation owner identifier (className, domainName, ...) used only to translate entities attributes
		 * 		{Object or String} identifier: entity's attribute/property identifier
		 * 		{String} field: field to translate (description, inverseDescription, ...),
		 * 		{CMDBuild.model.common.field.translatable.Window} translations
		 * 	}
		 */
		translationFieldConfig: {},

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			if (!Ext.Object.isEmpty(this.configurationGet())) {
				this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', {
					delegate: this
				});

				// Shorthands
				this.form = this.view.form;

				this.buildTranslationsFields();

				// Show window
				if (!Ext.isEmpty(this.view))
					this.view.show();
			} else {
				_warning('no field configuration', this);
			}
		},

		buildTranslationsFields: function() {
			var enabledLanguagesObjects = Ext.Object.getValues(CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].getEnabledLanguages());

			// Sort languages with description alphabetical order
			CMDBuild.core.Utils.objectArraySort(enabledLanguagesObjects);

			Ext.Array.forEach(enabledLanguagesObjects, function(language, i, allLanguages) {
				if (!Ext.isEmpty(this.form)) {
					this.form.add(
						Ext.create('Ext.form.field.Text', {
							name: language.get(CMDBuild.core.proxy.Constants.TAG),
							fieldLabel: language.get(CMDBuild.core.proxy.Constants.DESCRIPTION),
							labelWidth: CMDBuild.LABEL_WIDTH,
							padding: '3 5',
							labelClsExtra: 'ux-flag-' + language.get(CMDBuild.core.proxy.Constants.TAG),
							labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
						})
					);
				}
			}, this);

			this.view.center(); // AutoHeight windows won't be at the center of viewport on show, manually do it
		},

		// Configuration methods
			/**
			 * @param {Boolean} withTranslationsObject
			 * @param {Boolean} translationsObjectEncoded
			 *
			 * @returns {Object} decodedConfigurationObject
			 */
			configurationGet: function(withTranslationsObject, translationsObjectEncoded) {
				withTranslationsObject = withTranslationsObject || false;
				translationsObjectEncoded = translationsObjectEncoded || false;

				var decodedConfigurationObject = {};

				if (
					Ext.isObject(this.translationFieldConfig)
					&& !Ext.Object.isEmpty(this.translationFieldConfig)
				) {
					decodedConfigurationObject = {};
					decodedConfigurationObject[CMDBuild.core.proxy.Constants.TYPE] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.TYPE];
					decodedConfigurationObject[CMDBuild.core.proxy.Constants.OWNER] = this.decodeConfigurationValue(CMDBuild.core.proxy.Constants.OWNER);
					decodedConfigurationObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.decodeConfigurationValue(CMDBuild.core.proxy.Constants.IDENTIFIER);
					decodedConfigurationObject[CMDBuild.core.proxy.Constants.FIELD] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.FIELD];

					if (withTranslationsObject)
						decodedConfigurationObject[CMDBuild.core.proxy.Constants.TRANSLATIONS] = this.translationsGet(translationsObjectEncoded);
				}

				return decodedConfigurationObject;
			},

			/**
			 * @returns {Boolean}
			 */
			isConfigurationValid: function() {
				var configuration = this.configurationGet();

				return (
					!Ext.Object.isEmpty(configuration)
					&& !Ext.isEmpty(configuration[CMDBuild.core.proxy.Constants.TYPE])
					&& !Ext.isEmpty(configuration[CMDBuild.core.proxy.Constants.IDENTIFIER])
					&& !Ext.isEmpty(configuration[CMDBuild.core.proxy.Constants.FIELD])
				);
			},

		/**
		 * Decode object configuration values to get data from form
		 *
		 * @param {String} configurationKey
		 *
		 * @returns {String or null} decodedValue
		 */
		decodeConfigurationValue: function(configurationKey) {
			var decodedValue = configurationKey;

			if (!Ext.isEmpty(configurationKey)) {
				var configurationValue = this.translationFieldConfig[configurationKey];

				decodedValue = configurationValue;

				if(
					Ext.isObject(configurationValue)
					&& configurationValue.hasOwnProperty('sourceType')
					&& configurationValue.hasOwnProperty('key')
					&& configurationValue.hasOwnProperty('source')
				) {
					switch (configurationValue.sourceType) {
						case 'form': {
							if(!Ext.isEmpty(configurationValue.source) && Ext.isFunction(configurationValue.source.getData)) {
								decodedValue = configurationValue.source.getData(true)[configurationValue.key];
							} else {
								_error('form getData() function not implemented', this);
							}
						} break;

						case 'model': {
							if(!Ext.isEmpty(configurationValue.source) && Ext.isFunction(configurationValue.source.get)) {
								decodedValue = configurationValue.source.get(configurationValue.key);
							} else {
								_error('model get() function not implemented', this);
							}
						} break;

						case 'object': {
							if(configurationValue.source.hasOwnProperty(configurationValue.key) && Ext.isObject(configurationValue.source)) {
								decodedValue = configurationValue.source[configurationValue.key];
							} else {
								_error('object declared source is not an object', this);
							}
						} break;

						default: {
							_error('type not supported', this);
						}
					}
				}
			}

			return decodedValue;
		},

		onTranslatableWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Build fields with translations refreshing all data
		 */
		onTranslatableWindowBeforeShow: function() {
			this.setViewTitle();

			if (this.isConfigurationValid()) {
				// Get translations object from buffer
				if (this.buffer.hasOwnProperty(this.translationFieldConfig.identifier))
					this.translationsSet(this.buffer[this.translationFieldConfig.identifier][CMDBuild.core.proxy.Constants.TRANSLATIONS]);

				this.form.reset();

				if (this.translationsGet().isEmpty()) {
					CMDBuild.core.proxy.localizations.Localizations.read({
						params: this.configurationGet(),
						scope: this,
						success: function(response, options, decodedResponse) {
							this.translationsSet(decodedResponse.response);

							this.form.loadRecord(this.translationsGet());
						}
					});
				} else {
					this.form.loadRecord(this.translationsGet());
				}
			}
		},

		/**
		 * Bufferize translations to save on card save
		 */
		onTranslatableWindowConfirmButtonClick: function() {
			this.translationsSet(this.form.getValues());

			this.buffer[this.translationFieldConfig.identifier] = this.translationFieldConfig;
			this.buffer[this.translationFieldConfig.identifier][CMDBuild.core.proxy.Constants.TRANSLATIONS] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS].getData();

			this.onTranslatableWindowAbortButtonClick();
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (Ext.isEmpty(titlePart)) {
				this.view.setTitle(this.view.baseTitle);
			} else {
				this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
			}
		},

		// Translation method
			/**
			 * @param {Boolean} encoded
			 *
			 * @returns {CMDBuild.model.common.field.translatable.Window}
			 */
			translationsGet: function(encoded) {
				encoded = encoded || false;

				if (
					!Ext.isEmpty(this.translationFieldConfig)
					&& this.translationFieldConfig.hasOwnProperty(CMDBuild.core.proxy.Constants.TRANSLATIONS)
				) {
					if (encoded)
						return Ext.encode(this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS].getData());

					return this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS];
				}

				return Ext.create('CMDBuild.model.common.field.translatable.Window');
			},

			translationsRead: function() {
				if (this.isConfigurationValid()) {
					CMDBuild.core.proxy.localizations.Localizations.read({
						params: this.configurationGet(),
						scope: this,
						success: function(response, options, decodedResponse) {
							this.translationsSet(decodedResponse.response);
						}
					});
				}
			},

			/**
			 * @param {Object} translationsObject
			 */
			translationsSet: function(translationsObject) {
				this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS] = Ext.create('CMDBuild.model.common.field.translatable.Window', translationsObject);
			}
	});

})();