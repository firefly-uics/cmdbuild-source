(function () {

	Ext.define('CMDBuild.view.management.widget.customForm.layout.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.layout.Grid}
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

		/**
		 * @override
		 */
		initComponent: function () {
			var isWidgetReadOnly = this.delegate.cmfg('widgetCustomFormConfigurationGet', [
				CMDBuild.core.constants.Proxy.CAPABILITIES,
				CMDBuild.core.constants.Proxy.READ_ONLY
			]);

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addRow,
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
										CMDBuild.core.constants.Proxy.CAPABILITIES,
										CMDBuild.core.constants.Proxy.ADD_DISABLED
									])
								),

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutGridAddRowButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Import', {
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
										CMDBuild.core.constants.Proxy.CAPABILITIES,
										CMDBuild.core.constants.Proxy.IMPORT_DISABLED
									])
								),

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutGridImportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Export', {
								scope: this,

								disabled: (
									isWidgetReadOnly
									|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
										CMDBuild.core.constants.Proxy.CAPABILITIES,
										CMDBuild.core.constants.Proxy.EXPORT_DISABLED
									])
								),

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutGridExportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Reload', {
								text: CMDBuild.Translation.resetToDefault,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormResetButtonClick');
								}
							})
						]
					})
				],
				plugins: (
					isWidgetReadOnly
					|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
						CMDBuild.core.constants.Proxy.CAPABILITIES,
						CMDBuild.core.constants.Proxy.MODIFY_DISABLED
					])
				) ? [] : [this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })]
			});

			this.callParent(arguments);
		}
	});

})();
