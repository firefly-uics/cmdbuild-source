(function() {

	Ext.define('CMDBuild.controller.common.field.slider.SingleWithExtremeLabels', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onSliederChange',
		],

		/**
		 * @property {CMDBuild.view.common.field.slider.SingleWithExtremeLabels}
		 */
		view: undefined,

		onSliederChange: function() {
			var sliderValue = this.view.sliderField.getValue();

			if (Ext.isNumber(sliderValue) && !Ext.isEmpty(sliderValue))
				this.view.textField.setValue(sliderValue);
		}
	});

})();