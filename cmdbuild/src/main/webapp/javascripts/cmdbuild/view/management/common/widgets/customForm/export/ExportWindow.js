(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.export.ExportWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.Export}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.export.FormPanel}
		 */
		form: undefined,

		autoHeight: true,
		border: false,
		defaultSizeW: 0.90,
		title: CMDBuild.Translation.exportLabel,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Export', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onWidgetCustomFormExportExportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onWidgetCustomFormExportAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('CMDBuild.view.management.common.widgets.customForm.export.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.width = this.width * this.defaultSizeW;
		}
	});

})();