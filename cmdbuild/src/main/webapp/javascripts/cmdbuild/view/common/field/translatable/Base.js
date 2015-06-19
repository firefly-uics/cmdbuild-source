(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Base', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {Boolean}
		 */
		considerAsFieldToDisable: true,

		/**
		 * @property {CMDBuild.core.buttons.FieldTranslation}
		 */
		translationButton: undefined,

		/**
		 * Field translation properties
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
					decodedConfigurationObject[CMDBuild.core.proxy.Constants.FIELD] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.FIELD];

					if(Ext.isObject(this.translationFieldConfig[CMDBuild.core.proxy.Constants.OWNER])) {
						decodedConfigurationObject[CMDBuild.core.proxy.Constants.OWNER] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.OWNER].form.getData(true)[this.translationFieldConfig[CMDBuild.core.proxy.Constants.OWNER].key];
					} else {
						decodedConfigurationObject[CMDBuild.core.proxy.Constants.OWNER] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.OWNER];
					}

					if(Ext.isObject(this.translationFieldConfig[CMDBuild.core.proxy.Constants.OWNER])) {
						decodedConfigurationObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.IDENTIFIER].form.getData(true)[this.translationFieldConfig[CMDBuild.core.proxy.Constants.IDENTIFIER].key];
					} else {
						decodedConfigurationObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.translationFieldConfig[CMDBuild.core.proxy.Constants.IDENTIFIER];
					}

					if (withTranslationsObject)
						decodedConfigurationObject[CMDBuild.core.proxy.Constants.TRANSLATIONS] = this.translationsGet(translationsObjectEncoded);
				}

				return decodedConfigurationObject;
			},

			/**
			 * @param {Object} configurationObject
			 */
			configurationSet: function(configurationObject) {
				if (Ext.isObject(configurationObject)) {
					this.translationFieldConfig = configurationObject;

					// Read field's translations
					if (this.isConfigurationValid()) {
						CMDBuild.core.proxy.localizations.Localizations.read({
							params: this.configurationGet(),
							scope: this,
							success: function(response, options, decodedResponse) {
								this.translationsSet(decodedResponse.response);
							}
						});
					}
				}
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