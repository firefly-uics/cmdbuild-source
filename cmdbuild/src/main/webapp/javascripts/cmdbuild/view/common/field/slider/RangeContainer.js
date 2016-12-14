(function () {

	Ext.define('CMDBuild.view.common.field.slider.RangeContainer', {
		extend: 'Ext.container.Container',

		/**
		 * @cfg {CMDBuild.controller.common.field.slider.RangeContainer}
		 */
		delegate: undefined,

		/**
		 * @cfg {Ext.slider.Single}
		 */
		fieldMax: undefined,

		/**
		 * @cfg {Ext.slider.Single}
		 */
		fieldMin: undefined,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			// Error handling
				if (!Ext.isObject(this.fieldMax) || Ext.Object.isEmpty(this.fieldMax))
					return _error('initComponent(): unmanaged fieldMax property', this, this.fieldMax);

				if (!Ext.isObject(this.fieldMin) || Ext.Object.isEmpty(this.fieldMin))
					return _error('initComponent(): unmanaged fieldMin property', this, this.fieldMin);
			// END: Error handling

			Ext.apply(this, {
				items: [
					this.fieldMax,
					this.fieldMin
				]
			});

			this.delegate = Ext.create('CMDBuild.controller.common.field.slider.RangeContainer', { view: this });

			this.callParent(arguments);

			this.fieldMax.on('drag', function (field, e, eOpts) {
				this.delegate.cmfg('onFieldSliderRangeContainerFieldMaxDrag');
			}, this);

			this.fieldMin.on('drag', function (field, e, eOpts) {
				this.delegate.cmfg('onFieldSliderRangeContainerFieldMinDrag');
			}, this);
		},

		/**
		 * @returns  {Void}
		 */
		disable: function () {
			this.delegate.cmfg('fieldSliderRangeContainerDisable');
		},

		/**
		 * @returns  {Void}
		 */
		enable: function () {
			this.delegate.cmfg('fieldSliderRangeContainerEnable');
		}
	});

})();
