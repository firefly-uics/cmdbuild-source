(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Base', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.proxy.Localizations'],

		/**
		 * @cfg {Boolean}
		 */
		allowBlank: true,

		/**
		 * @cfg {Boolean}
		 */
		considerAsFieldToDisable: true,

		/**
		 * @property {CMDBuild.core.buttons.FieldTranslation}
		 */
		translationButton: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyField: undefined,

		translationsKeyName: undefined,
		translationsKeySubName: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyType: undefined,

		layout: 'hbox',

		initComponent: function() {
			this.field = this.createField();

			if (CMDBuild.Config.localization.hasLocalizations())
				this.translationButton = Ext.create('CMDBuild.core.buttons.FieldTranslation', {
					scope: this,

					handler: function(button, e) {
						Ext.create('CMDBuild.controller.common.field.translatable.Window', {
							translationsKeyType: this.translationsKeyType,
							translationsKeyName: this.translationsKeyName,
							translationsKeySubName: this.translationsKeySubName,
							translationsKeyField: this.translationsKeyField
						});
					}
				});

			Ext.apply(this, {
				items: [this.field, this.translationButton]
			});

			this.callParent(arguments);
		},

		/**
		 * @abstract
		 */
		createField: function() {},

		/**
		 * Forward method
		 *
		 * @return {String}
		 */
		getValue: function() {
			return this.field.getValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Boolean}
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
		}
	});

})();