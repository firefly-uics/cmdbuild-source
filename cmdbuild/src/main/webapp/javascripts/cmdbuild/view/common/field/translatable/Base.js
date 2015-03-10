(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Base', {
		extend: 'Ext.form.FieldContainer',

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

			if (_CMCache.isMultiLanguages())
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

			_CMCache.registerOnTranslations(this);

			this.callParent(arguments);
		},

		/**
		 * @abstract
		 */
		createField: function() {},

		/**
		 * @return {String}
		 */
		getValue: function() {
			return this.field.getValue();
		},

		/**
		 * @return {Boolean}
		 */
		isValid: function() {
			return this.field.isValid();
		},

		/**
		 * @param {String} value
		 */
		setValue: function(value) {
			this.field.setValue(value);
		},

		reset: function() {
			this.field.reset();
		}
	});

})();