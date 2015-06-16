(function() {

	Ext.define('CMDBuild.controller.common.field.translatable.Window', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTranslatableWindowAbortButtonClick',
			'onTranslatableWindowConfirmButtonClick'
		],

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyField: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyName: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeySubName: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyType: undefined,

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

			var me = this;

			this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', {
				delegate: this
			});

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();

			_CMCache.readTranslations(
				this.translationsKeyType,
				this.translationsKeyName,
				this.translationsKeySubName,
				this.translationsKeyField,
				function(result, options, decodedResult) {
					me.form.oldValues = decodedResult.response;

					me.buildWindowItem(decodedResult.response);
				}
			);
		},

		/**
		 * @param {Object} translations
		 */
		buildWindowItem: function(translationsValues) {
			var enabledLanguages = CMDBuild.configuration[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].getEnabledLanguages();

			Ext.Object.each(enabledLanguages, function(key, value, myself) {
				var item = Ext.create('Ext.form.field.Text', {
					name: value.get(CMDBuild.core.proxy.CMProxyConstants.TAG),
					fieldLabel: value.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
					labelWidth: CMDBuild.LABEL_WIDTH,
					padding: '3 5',
					labelClsExtra: 'ux-flag-' + value.get(CMDBuild.core.proxy.CMProxyConstants.TAG),
					labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
				});

				item.setValue(
					translationsValues[value.get(CMDBuild.core.proxy.CMProxyConstants.TAG)]
				);

				if (!Ext.isEmpty(this.form))
					this.form.add(item);
			},this);
		},

		onTranslatableWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onTranslatableWindowConfirmButtonClick: function() {
			_CMCache.createTranslations(
				this.translationsKeyType,
				this.translationsKeyName,
				this.translationsKeySubName,
				this.translationsKeyField,
				this.form.getValues(),
				this.form.getOldValues() // Control for create, delete or update values
			);

			this.onTranslatableWindowAbortButtonClick();
		}
	});

})();