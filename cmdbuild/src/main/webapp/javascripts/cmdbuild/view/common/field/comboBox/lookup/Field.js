(function () {

	/**
	 * @private
	 */
	Ext.define('CMDBuild.view.common.field.comboBox.lookup.Field', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.lookup.Field}
		 */
		delegate: undefined,

		disablePanelFunctions: true,
		displayField: 'Description',
		hideTrigger1: false,
		hideTrigger2: false,
		queryMode: 'local',
		trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		valueField: 'Id',

		listeners: {
			focus: function (field, event, eOpts) {
				this.delegate.cmfg('onFieldComboBoxLookupFocus', this.delegate);
			},
			select: function (field, records, eOpts) {
				this.delegate.cmfg('onFieldComboBoxLookupSelect', this.delegate);
			},
			change: function (field, newValue, oldValue, eOpts) {
				this.delegate.cmfg('onFieldComboBoxLookupFieldChange');
			}
		},

		/**
		 * Compatibility with template resolver.
		 * Used by the template resolver to know if a field is a combo and to take the value of multilevel lookup
		 *
		 * @returns {String}
		 */
		getReadableValue: function () {
			return this.getRawValue();
		},

		/**
		 * @returns {Void}
		 */
		onTrigger1Click: function () {
			this.delegate.cmfg('onFieldComboBoxLookupFieldTrigger1Click');
		},

		/**
		 * @returns {Void}
		 */
		onTrigger2Click: function () {
			this.delegate.cmfg('onFieldComboBoxLookupFieldTrigger2Click');
		}
	});

})();
