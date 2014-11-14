(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMGrid', {
		extend: 'Ext.panel.Panel',

		statics: {
			WIDGET_NAME: '.Grid'
		},

		delegate: undefined,

		autoScroll: true,
		border: false,
		frame: false,

		layout: {
			type: 'border'
		},

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.addButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				text: CMDBuild.Translation.row_add,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onAddRowButtonClick');
				}
			});

			this.importFromCSVButton = Ext.create('Ext.button.Button', {
				iconCls: 'import',
				text: CMDBuild.Translation.importFromCSV,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onCSVImportButtonClick');
				}
			});

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.grid.CMGridPanel', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.addButton, this.importFromCSVButton]
					}
				],
				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();