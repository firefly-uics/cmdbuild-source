(function () {

	Ext.define('CMDBuild.view.common.field.filter.basic.Basic', {
		extend: 'Ext.form.field.Trigger',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.basic.Basic}
		 */
		delegate: undefined,

		/**
		 * @cfg {Ext.grid.Panel}
		 */
		grid: undefined,

		hideTrigger1: false,
		hideTrigger2: false,
		trigger1Cls: Ext.baseCSSPrefix + 'form-search-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		validateOnBlur: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.callParent(arguments);

			this.on('specialkey', function (f, e) {
				if (e.getKey() == e.ENTER)
					this.delegate.cmfg('onFieldFilterBasicTrigger1Click');
			}, this);
		},

		/**
		 * @returns {Void}
		 */
		onTrigger1Click: function () {
			this.delegate.cmfg('onFieldFilterBasicTrigger1Click');
		},

		/**
		 * @returns {Void}
		 */
		onTrigger2Click: function () {
			this.delegate.cmfg('onFieldFilterBasicTrigger2Click');
		},

		/**
		 * @param {Boolean} applyToStore
		 *
		 * @returns {Void}
		 */
		reset: function (applyToStore) {
			this.delegate.cmfg('onFieldFilterBasicReset', applyToStore);
		}
	});

})();
