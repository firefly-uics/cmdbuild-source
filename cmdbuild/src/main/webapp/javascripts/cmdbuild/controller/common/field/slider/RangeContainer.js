(function () {

	Ext.define('CMDBuild.controller.common.field.slider.RangeContainer', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldSliderRangeContainerDisable',
			'fieldSliderRangeContainerEnable',
			'onFieldSliderRangeContainerFieldMaxDrag',
			'onFieldSliderRangeContainerFieldMinDrag'
		],

		/**
		 * @cfg {CMDBuild.view.common.field.slider.RangeContainer}
		 */
		view: undefined,

		/**
		 * @returns {Void}
		 */
		fieldSliderRangeContainerDisable: function () {
			this.view.fieldMax.disable();
			this.view.fieldMin.disable();
		},

		/**
		 * @returns {Void}
		 */
		fieldSliderRangeContainerEnable: function () {
			this.view.fieldMax.enable();
			this.view.fieldMin.enable();
		},

		/**
		 * @returns {Void}
		 */
		onFieldSliderRangeContainerFieldMaxDrag: function () {
			if (this.view.fieldMax.getValue() < this.view.fieldMin.getValue())
				this.view.fieldMin.setValue(this.view.fieldMax.getValue());
		},

		/**
		 * @returns {Void}
		 */
		onFieldSliderRangeContainerFieldMinDrag: function () {
			if (this.view.fieldMin.getValue() > this.view.fieldMax.getValue())
				this.view.fieldMax.setValue(this.view.fieldMin.getValue());
		}
	});

})();
