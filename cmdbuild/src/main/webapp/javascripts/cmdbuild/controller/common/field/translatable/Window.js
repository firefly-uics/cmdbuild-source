(function () {

	Ext.define('CMDBuild.controller.common.field.translatable.Window', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Translatable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldTranslatableWindowAbortButtonClick',
			'onFieldTranslatableWindowConfigureAndShow',
			'onFieldTranslatableWindowConfirmButtonClick'
		],

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.translatable.Translatable} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		buildTranslationsFields: function () {
			var enabledLanguagesObjects = Ext.Object.getValues(CMDBuild.configuration.localization.getEnabledLanguages()),
				fields = [];

			this.form.removeAll();

			CMDBuild.core.Utils.objectArraySort(enabledLanguagesObjects); // Sort languages with description alphabetical order

			Ext.Array.forEach(enabledLanguagesObjects, function (language, i, allLanguages) {
				fields.push(
					Ext.create('Ext.form.field.Text', {
						name: language.get(CMDBuild.core.constants.Proxy.TAG),
						fieldLabel: language.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
						labelClsExtra: 'ux-flag-' + language.get(CMDBuild.core.constants.Proxy.TAG),
						labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;',
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
						padding: '3 5'
					})
				);
			}, this);

			if (!Ext.isEmpty(fields))
				this.form.add(fields);

			this.view.center(); // AutoHeight windows won't be at the center of viewport on show, manually do it
		},

		/**
		 * @returns {Void}
		 */
		onFieldTranslatableWindowAbortButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Object} parameters
		 * @returns {CMDBuild.controller.common.field.translatable.Translatable} parameters.parentDelegate
		 *
		 * @returns {Void}
		 */
		onFieldTranslatableWindowConfigureAndShow: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			Ext.apply(this, parameters);

			var translationsModel = this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.TRANSLATIONS);

			// Error handling
				if (this.cmfg('fieldTranslatableConfigurationIsEmpty') || !this.cmfg('fieldTranslatableConfigurationGet').isValid())
					return _error('constructor(): unmanaged configuration property', this, this.cmfg('fieldTranslatableConfigurationGet'));

				if (!Ext.isObject(translationsModel) || Ext.Object.isEmpty(translationsModel) || !Ext.isFunction(translationsModel.getData))
					return _error('constructor(): unmanaged configuration property', this, translationsModel);
			// END: Error handling

			this.setViewTitle(this.cmfg('fieldTranslatableFieldLabelGet'));

			this.buildTranslationsFields();

			this.form.loadRecord(translationsModel);

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * Buffer translations to save on card save
		 *
		 * @returns {Void}
		 */
		onFieldTranslatableWindowConfirmButtonClick: function () {
			this.cmfg('fieldTranslatableConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.TRANSLATIONS,
				value: Ext.create('CMDBuild.model.common.field.translatable.Window', this.form.panelFunctionDataGet())
			});

			this.cmfg('onFieldTranslatableWindowAbortButtonClick');
		}
	});

})();
