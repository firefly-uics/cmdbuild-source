(function() {

	Ext.define('CMDBuild.view.administration.dataView.filter.FilterView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Filter}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.dataView.filter.GridPanel', {
				delegate: this.delegate,
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.dataView.filter.FormPanel', {
				delegate: this.delegate,
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Add', {
								text: CMDBuild.Translation.addView, // TODO mettere in minuscolo nella traduzione
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewFilterAddButtonClick');
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