(function() {

	Ext.define('CMDBuild.view.management.report.ParametersWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.report.Parameters}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		autoHeight: true,
		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.management.modreport.report_parameters,

		initComponent: function() {
			this.form = Ext.create('Ext.form.Panel', {
				labelAlign: 'right',
				frame: true,
				border: false
			});

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
									this.delegate.cmfg('onParametersSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onParametersAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.form]
			});

			this.callParent(arguments);
		}
	});

})();