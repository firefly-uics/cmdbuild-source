(function() {

	Ext.define('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @property {CMDBuild.controller.common.field.slider.SingleWithTextField}
		 */
		delegate: undefined,

		/**
		 * @cfg {Number}
		 */
		maxValue: 100,

		/**
		 * @cfg {Number}
		 */
		minValue: 0,

		considerAsFieldToDisable: true,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
//			this.delegate = Ext.create('CMDBuild.controller.common.field.slider.SingleWithTextField', { // TODO delete if not used
//				view: this
//			});

			this.sliderField = Ext.create('Ext.slider.Single', {
				flex: 1,
				useTips: true,
				minValue: this.minValue,
				maxValue: this.maxValue,

//				listeners: {
//					scope: this,
//					change: function(slider, newValue, thumb, eOpts) {
//						this.delegate.cmfg('onSliederChange');
//					}
//				}
			});

//			this.textField = Ext.create('Ext.form.field.Text', {
//				padding: '0 0 0 5',
//				readOnly: true,
//				disabled: true,
//				width: 25
//			});

			Ext.apply(this, {
				items: [
//					this.textField,
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
