(function() {

	Ext.define('CMDBuild.view.management.reports.ParametersWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.reports.Parameters}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		autoHeight: true,
		autoWidth: true,
		border: false,
		frame: false,
		layout: 'fit',

		title: CMDBuild.Translation.reportParameters,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportParametersWindowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportParametersWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						labelAlign: 'right',
						frame: true,
						border: false,

						layout: {
							type: 'vbox',
							align: 'stretch'
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();