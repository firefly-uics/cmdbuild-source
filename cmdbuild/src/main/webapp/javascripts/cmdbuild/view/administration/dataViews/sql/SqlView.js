(function() {

	Ext.define('CMDBuild.view.administration.dataViews.sql.SqlView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataViews.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataViews.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataViews.sql.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.dataViews.sql.GridPanel', {
				delegate: this.delegate,
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.dataViews.sql.FormPanel', {
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
								text: CMDBuild.Translation.addView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewsSqlAddButtonClick');
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