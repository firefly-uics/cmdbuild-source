(function() {

	var tr = CMDBuild.Translation.administration.tasks.workflowForm;

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		title: tr.attributes,
		considerAsFieldToDisable: true,
		margin: '0 0 5 0',

		plugins: [
			Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1
			})
		],

		columns: [
			{
				header: CMDBuild.Translation.name,
				dataIndex: CMDBuild.core.proxy.CMProxyConstants.NAME,
				flex: 1,

				editor: { xtype: 'combo' }
			},
			{
				header: CMDBuild.Translation.value,
				dataIndex: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				flex: 1,

				editor: { xtype: 'textfield' }
			},
			{
				xtype: 'actioncolumn',
				width: 30,
				align: 'center',
				sortable: false,
				hideable: false,
				menuDisabled: true,
				fixed: true,

				items: [
					{
						icon: 'images/icons/cross.png',
						tooltip: CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove,
						handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
							grid.store.remove(record);
						}
					}
				]
			}
		],

		store: Ext.create('Ext.data.Store', {
			model: 'CMDBuild.model.CMModelTasks.common.workflowForm',
			data: []
		}),

		initComponent: function() {
			var me = this;

			this.tbar = [
				{
					text: CMDBuild.Translation.common.buttons.add,
					iconCls: 'add',
					handler: function() {
						me.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.common.workflowForm'));
					}
				}
			];

			this.callParent(arguments);
		}
	});

})();
