(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.layout.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

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
			var isWidgetReadOnly = this.delegate.cmfg('widgetConfigurationGet', [
				CMDBuild.core.proxy.Constants.CAPABILITIES,
				CMDBuild.core.proxy.Constants.READ_ONLY
			]);

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addRow,
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.Constants.CAPABILITIES,
										CMDBuild.core.proxy.Constants.ADD_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutGridAddRowButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Import', {
								text: CMDBuild.Translation.import,
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetConfigurationGet', [
										CMDBuild.core.proxy.Constants.CAPABILITIES,
										CMDBuild.core.proxy.Constants.IMPORT_DISABLED
									])
								),

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutGridImportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Reload', {
								text: CMDBuild.Translation.resetToDefault,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onCustomFormLayoutGridResetButtonClick');
								}
							})
						]
					})
				],
				plugins: (
					isWidgetReadOnly
					|| this.delegate.cmfg('widgetConfigurationGet', [
						CMDBuild.core.proxy.Constants.CAPABILITIES,
						CMDBuild.core.proxy.Constants.MODIFY_DISABLED
					])
				) ? [] : [this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })]
			});

			this.callParent(arguments);
		}
	});

})();