(function() {

	Ext.define('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Number}
		 */
		maxValue: 100,

		/**
		 * @cfg {Number}
		 */
		minValue: 0,

		/**
		 * @property {Ext.slider.Single}
		 */
		sliderField: undefined,

		considerAsFieldToDisable: true,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			this.sliderField = Ext.create('Ext.slider.Single', {
				flex: 1,
				useTips: true,
				minValue: this.minValue,
				maxValue: this.maxValue
			});

			Ext.apply(this, {
				items: [
					{
						xtype: 'displayfield',
						padding: '0 5',
						value: this.minValue
					},
					this.sliderField,
					{
						xtype: 'displayfield',
						padding: '0 5',
						value: this.maxValue
					}
				]
			});

			this.callParent(arguments);
		},

		/**
		 * Forward method
		 *
		 * @return {String}
		 */
		getRawValue: function() {
			return this.sliderField.getRawValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Number}
		 */
		getValue: function() {
			return this.sliderField.getValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Boolean}
		 */
		isValid: function() {
			return this.sliderField.isValid();
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabled: function(state) {
			this.sliderField.setDisabled(state);
		},

		/**
		 * Forward method
		 *
		 * @param {Number} value
		 */
		setValue: function(value) {
			return this.sliderField.setValue(value);
		},

		/**
		 * Forward method
		 */
		reset: function() {
			this.sliderField.reset();
		},
	});

})();
