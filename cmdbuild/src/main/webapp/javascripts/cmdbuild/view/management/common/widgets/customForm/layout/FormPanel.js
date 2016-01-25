(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.layout.Form}
		 */
		delegate: undefined,

		bodyCls: 'x-panel-body-default-framed',
		bodyPadding: 5,
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			var isWidgetReadOnly = this.delegate.cmfg('widgetCustomFormConfigurationGet', [
				CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
				CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
			]);

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Import', {
								text: CMDBuild.Translation.import,
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
										CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
										CMDBuild.core.proxy.CMProxyConstants.IMPORT_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutFormImportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Export', {
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
										CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
										CMDBuild.core.proxy.CMProxyConstants.EXPORT_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutFormExportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Reload', {
								text: CMDBuild.Translation.resetToDefault,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutFormResetButtonClick');
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