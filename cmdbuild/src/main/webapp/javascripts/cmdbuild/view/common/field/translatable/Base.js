(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Base', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {Boolean}
		 */
		considerAsFieldToDisable: true,

		/**
		 * @property {Mixed}
		 */
		field: undefined,

		/**
		 * @property {CMDBuild.core.buttons.FieldTranslation}
		 */
		translationButton: undefined,

		/**
		 * Field translation properties.
		 * NOTE: owner and identifier could be objects (key, form) to use with getData() to get data from server
		 *
		 * @cfg {Object}
		 *
		 * Ex. {
		 * 		type: entity type identifier (class, attributeclass, domain, attributedomain, filter, instancename, lookupvalue, menuitem, report, view, classwidget)
		 * 		owner: translation owner identifier (className, domainName, ...) used only to translate entities attributes
		 * 		identifier: entity's attribute/property identifier
		 * 		field: field to translate (description, inverseDescription, ...)
		 * 	}
		 */
		translationFieldConfig: {},

		allowBlank: true,
		layout: 'hbox',

		initComponent: function() {
			this.field = this.createField();

			if (CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].hasEnabledLanguages()) {
				this.translationButton = Ext.create('CMDBuild.core.buttons.FieldTranslation', {
					scope: this,

					handler: function(button, e) {
						Ext.create('CMDBuild.controller.common.field.translatable.Window', {
							ownerField: this
						});
					}
				});
			}

			Ext.apply(this, {
				items: [this.field, this.translationButton]
			});

			this.callParent(arguments);
		},

		listeners: {
			// Read field's translations
			enable: function(field, eOpts) {
				if (field.isConfigurationValid()) {
					CMDBuild.core.proxy.localizations.Localizations.read({
						params: field.configurationGet(),
						success: function(response, options, decodedResponse) {
							field.translationsSet(decodedResponse.response);
						}
					});
				}
			}
		},

		/**
		 * @abstract
		 */
		createField: Ext.emptyFn,

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
					&& !Ext.isEmpty(configuration[CMDBuild.core.proxy.Constants.OWNER])
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

				if(
					Ext.isObject(configurationValue)
					&& !Ext.isEmpty(configurationValue.form)
					&& !Ext.isEmpty(configurationValue.key)
					&& Ext.isFunction(configurationValue.form.getData)
				) {
					decodedValue = configurationValue.form.getData(true)[configurationValue.key];
				} else {
					decodedValue = configurationValue;
				}
			}

			return decodedValue;
		},

		/**
		 * Forward method
		 *
		 * @returns {String}
		 */
		getValue: function() {
			return this.field.getValue();
		},

		/**
		 * Forward method
		 *
		 * @returns {Boolean}
		 */
		isValid: function() {
			return this.field.isValid();
		},

		/**
		 * Forward method
		 *
		 * @param {String} value
		 */
		setValue: function(value) {
			this.field.setValue(value);
		},

		/**
		 * Forward method
		 */
		reset: function() {
			this.field.reset();
		},

		// Translation method
			/**
			 * @param {Boolean} encoded
			 */
			translationsGet: function(encoded) {
				encoded = encoded || false;

				if (encoded)
					return Ext.encode(this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS]);

				return this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS];
			},

			/**
			 * @param {Object} translationsObject
			 */
			translationsSet: function(translationsObject) {
				if (Ext.Object.isEmpty(translationsObject)) {
					this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS] = {};
				} else {
					this.translationFieldConfig[CMDBuild.core.proxy.Constants.TRANSLATIONS] = translationsObject;
				}
			}
	});

})();