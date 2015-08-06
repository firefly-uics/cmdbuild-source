(function() {

	Ext.define('CMDBuild.view.administration.reports.jasper.JasperView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.reports.Jasper}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.reports.jasper.form.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.reports.jasper.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addReport,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportsJasperAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.reports.jasper.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.reports.jasper.form.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();