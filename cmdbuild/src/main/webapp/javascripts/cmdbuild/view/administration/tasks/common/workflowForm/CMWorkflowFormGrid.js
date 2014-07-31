(function() {

	var tr = CMDBuild.Translation.administration.tasks.workflowForm;

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		title: tr.attributes,
		considerAsFieldToDisable: true,
		margin: '0 0 5 0',
		minWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,

		columns: [
			{
				header: CMDBuild.Translation.name,
				dataIndex: CMDBuild.core.proxy.CMProxyConstants.NAME,
				flex: 1,

				editor: { xtype: 'textfield' }
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
						tooltip: CMDBuild.Translation.common.buttons.remove,
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

			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1,

				listeners: {
					beforeedit: function(editor, e, eOpts) {
						me.delegate.cmOn('onBeforeEdit', {
							fieldName: e.field,
							rowData: e.record.data
						});
					}
				}
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [
							{
								text: CMDBuild.Translation.common.buttons.add,
								iconCls: 'add',
								handler: function() {
									me.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.common.workflowForm'));
								}
							}
						]
					}
				],
				plugins: [this.gridEditorPlugin]
			});

			this.callParent(arguments);
		}
	});

})();
