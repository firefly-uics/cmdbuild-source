(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.layout.Form}
		 */
		delegate: undefined,

		bodyCls: 'x-panel-body-default-framed',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
//			align: 'stretch' // TODO: uncomment on new fieldManager full implementation
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Import', {
								text: CMDBuild.Translation.import,
								scope: this,

								disabled: (
									this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.Constants.CAPABILITIES,
										CMDBuild.core.proxy.Constants.READ_ONLY
									])
									|| this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.Constants.CAPABILITIES,
										CMDBuild.core.proxy.Constants.IMPORT_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutFormImportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Reload', {
								text: CMDBuild.Translation.resetToDefault,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutFormResetButtonClick');
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