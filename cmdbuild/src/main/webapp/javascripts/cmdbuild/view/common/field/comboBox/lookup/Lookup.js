(function () {

	Ext.define('CMDBuild.view.common.field.comboBox.lookup.Lookup', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @property {CMDBuild.controller.common.field.comboBox.lookup.Lookup}
		 */
		delegate: undefined,

		/**
		 * @cfg {CMDBuild.model.common.attributes.Attribute}
		 */
		attributeModel: undefined,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function () {
			this.callParent(arguments);

			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.comboBox.lookup.Lookup', { view: this })
			});
		},

		getRawValue: function() { // TODO: compatibility with template resolver
			var out = "";
			this.items.each(function(subField, index) {
				if (subField !== this.hiddenField) {
					if (index > 0) {
						out += " - ";
					}
					out += subField.getRawValue();
				}
			});

			return out;
		},

		/**
		 * @returns {Number or null}
		 */
		getValue: function () {
			return this.delegate.cmfg('fieldComboBoxLookupValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('fieldComboBoxLookupIsValid');
		},

		/**
		 * @param {Number} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('fieldComboBoxLookupValueSet', value);
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('fieldComboBoxLookupReset');
		}
	});

})();
