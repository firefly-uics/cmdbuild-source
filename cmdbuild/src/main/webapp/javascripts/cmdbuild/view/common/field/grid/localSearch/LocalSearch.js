(function () {

	Ext.define('CMDBuild.view.common.field.grid.localSearch.LocalSearch', {
		extend: 'Ext.form.field.Trigger',

		/**
		 * @cfg {CMDBuild.controller.common.field.grid.localSearch.LocalSearch}
		 */
		delegate: undefined,

		/**
		 * @cfg {Ext.grid.Panel}
		 */
		grid: undefined,

		enableKeyEvents: true,
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
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.grid.localSearch.LocalSearch', { view: this })
			});

			this.callParent(arguments);
		},

		listeners: {
			specialkey: function (field, e, eOpts) {
				if (e.getKey() == e.ENTER)
					this.delegate.cmfg('onFieldGridLocalSearchEnterKeyPress');
			}
		},

		/**
		 * @returns {Void}
		 */
		onTrigger1Click: function () {
			this.delegate.cmfg('fieldGridLocalSearchTrigger1Click');
		},

		/**
		 * @returns {Void}
		 */
		onTrigger2Click: function () {
			this.delegate.cmfg('fieldGridLocalSearchTrigger2Click');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('fieldGridLocalSearchReset');
		}
	});

})();
