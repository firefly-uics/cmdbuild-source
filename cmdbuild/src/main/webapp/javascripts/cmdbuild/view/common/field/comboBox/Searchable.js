(function() {

	Ext.define('CMDBuild.view.common.field.comboBox.Searchable', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.Searchable}
		 */
		delegate: undefined,

		/**
		 * @cfg {CMDBuild.model.common.attributes.Attribute}
		 */
		attributeModel: undefined,

		/**
		 * @cfg {Object}
		 */
		configuration: {},

		hideTrigger1: false,
		hideTrigger2: false,
		hideTrigger3: false,
		labelAlign: 'right',
		trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		trigger3Cls: Ext.baseCSSPrefix + 'form-search-trigger',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.controller.common.field.comboBox.Searchable', { view: this });

			this.callParent(arguments);
		},

		/**
		 * Compatibility with template resolver.
		 * Used by the template resolver to know if a field is a combo and to take the value of multilevel lookup
		 *
		 * @returns {String}
		 */
		getReadableValue: function() {
			return this.getRawValue();
		},

		/**
		 * @override
		 */
		onKeyUp: function() {
			if (this.delegate.cmfg('fieldComboBoxSearchableStoreExceedsLimit')) {
				this.delegate.cmfg('onFieldComboBoxSearchableKeyUp');
			} else {
				this.callParent(arguments);
			}
		},

		onTrigger1Click: function() {
			this.delegate.cmfg('onFieldComboBoxSearchableTrigger1Click');
		},

		onTrigger2Click: function() {
			this.delegate.cmfg('onFieldComboBoxSearchableTrigger2Click');
		},

		onTrigger3Click: function(value) {
			this.delegate.cmfg('onFieldComboBoxSearchableTrigger3Click');
		},

		/**
		 * Adds values in store if not already inside
		 *
		 * @param {String} value
		 *
		 * @override
		 */
		setValue: function (value) {
			if (this.getStore().find(this.valueField, value) >= 0) {
				return this.callParent(arguments);
			} else {
				return this.delegate.cmfg('onFieldComboBoxSearchableSetValue', value);
			}
		}
	});

})();