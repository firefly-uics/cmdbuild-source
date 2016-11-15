(function() {

	Ext.define('CMDBuild.view.common.field.translatable.window.Window', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Window}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.translations,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		autoHeight: true,
		autoWidth: true,
		autoScroll: true,
		border: false,
		dimensionsMode: 'absolute',
		frame: false,
		layout: 'fit',

		dimensions: {
			height: 'auto',
			width: 800
		},

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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onTranslatableWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onTranslatableWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('CMDBuild.view.common.field.translatable.window.FormPanel', {
						delegate: this.delegate
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			beforeshow: function(window, eOpts) {
				this.delegate.cmfg('onTranslatableWindowBeforeShow');
			}
		}
	});

})();