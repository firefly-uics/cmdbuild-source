(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.RowEditWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.layout.Grid}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		border: false,
		defaultSizeW: 0.90,
		defaultSizeH: 0.80,
		title: CMDBuild.Translation.editRow,

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
									this.delegate.cmfg('onRowEditWindowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onRowEditWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						border: false,
						frame: true,
						overflowY: 'auto',

						layout: {
							type: 'vbox',
//							align: 'stretch' // TODO: uncomment on new fieldManager full implementation
						}
					})
				]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		}
	});

})();