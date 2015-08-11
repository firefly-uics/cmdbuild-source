(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.layout.GridPanel', {
		extend: 'Ext.grid.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.layout.Grid}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		columns: [],

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Add', {
								text: CMDBuild.Translation.addRow,
								scope: this,

								disabled: (
									this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
										CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
									])
									|| this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
										CMDBuild.core.proxy.CMProxyConstants.ADD_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutGridAddRowButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Import', {
								text: CMDBuild.Translation.importFromCSV,
								scope: this,

								disabled: (
									this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
										CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
									])
									|| this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
										CMDBuild.core.proxy.CMProxyConstants.IMPORT_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutGridCSVImportButtonClick');
								}
							})
						]
					})
				],
				plugins: [this.gridEditorPlugin]
			});

			this.callParent(arguments);
		}
	});

})();