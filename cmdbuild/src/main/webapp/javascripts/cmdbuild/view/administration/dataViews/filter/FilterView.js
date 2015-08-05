(function() {

	Ext.define('CMDBuild.view.administration.dataViews.filter.FilterView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataViews.Filter}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataViews.filter.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataViews.filter.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.dataViews.filter.GridPanel', {
				delegate: this.delegate,
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.dataViews.filter.FormPanel', {
				delegate: this.delegate,
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Add', {
								text: CMDBuild.Translation.addView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewsFilterAddButtonClick');
								}
							})
						]
					})
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();