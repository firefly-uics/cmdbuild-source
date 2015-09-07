(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.import.ImportWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.Import}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.import.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {Boolean}
		 */
		modeDisabled: false,

		autoHeight: true,
		border: false,
		defaultSizeW: 0.90,
		title: CMDBuild.Translation.import,

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
							Ext.create('CMDBuild.core.buttons.Upload', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormImportUploadButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormImportAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('CMDBuild.view.management.common.widgets.customForm.import.FormPanel', {
						delegate: this.delegate,
						modeDisabled: this.modeDisabled
					})
				]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.width = this.width * this.defaultSizeW;
		}
	});

})();