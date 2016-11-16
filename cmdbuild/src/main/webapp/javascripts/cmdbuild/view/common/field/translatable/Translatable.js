(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.view.common.field.translatable.Translatable', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @property {CMDBuild.controller.common.field.translatable.Translatable}
		 */
		delegate: undefined,

		/**
		 * NOTE: owner and identifier could be objects (key, form) to use with getData() to get data from server
		 *
		 * @cfg {Object}
		 *
		 * Ex. {
		 * 		{String} fieldMode: text
		 * 		{String} type: entity type identifier (class, attributeclass, domain, attributedomain, filter, instancename, lookupvalue, menuitem, report, view, classwidget)
		 * 		{Object or String} owner: translation owner identifier (className, domainName, ...) used only to translate attribute's entities
		 * 		{Object or String} identifier: entity's attribute/property identifier
		 * 		{String} field: field to translate (description, inverseDescription, ...),
		 * 		{CMDBuild.model.common.field.translatable.Window} translations
		 * 	}
		 */
		config: {},

		/**
		 * @property {Object}
		 */
		field: undefined,

		border: false,
		frame: false,
		layout: 'hbox',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.common.field.translatable.Translatable', { view: this });

			Ext.apply(this, {
				items: [
					this.field = this.delegate.cmfg('fieldTranslatableBuildField'),
					CMDBuild.configuration.localization.hasEnabledLanguages() ? Ext.create('CMDBuild.core.buttons.FieldTranslation', {
						margin: '0 0 0 5',
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onFieldTranslatableButtonClick');
						}
					}) : null
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			enable: function(field, eOpts) {
				this.delegate.cmfg('fieldTranslatableConfigurationReadTranslations'); // Read field's translations
			}
		},

		/**
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 */
		configurationSet: function (configurationObject) {
			this.delegate.cmfg('fieldTranslatableConfigurationSet', { value: configurationObject });
		},

		/**
		 * @returns {String}
		 */
		getValue: function () {
			return this.delegate.cmfg('fieldTranslatableValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('fieldTranslatableIsValid');
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Object}
		 */
		paramsGet: function (parameters) {
			return this.delegate.cmfg('fieldTranslatableParamsGet', parameters);
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('fieldTranslatableReset');
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('fieldTranslatableValueSet', value);
		}
	});

})();