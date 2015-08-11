(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', {
		extend: 'Ext.form.Panel',

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.layout.Form}
		 */
		delegate: undefined,

//		importButton = Ext.create('CMDBuild.core.buttons.Import

		border: false,
		frame: false,
		overflowY: 'auto',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							this.importButton = Ext.create('CMDBuild.core.buttons.Import', {
								text: CMDBuild.Translation.importFromCSV,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutFormCSVImportButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();