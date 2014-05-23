(function() {

	var tr = CMDBuild.Translation.administration.tasks.workflowForm;

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', {
		extend: 'CMDBuild.view.administration.common.CMDynamicKeyValueGrid',

		delegate: undefined,

		title: tr.attributes,
		considerAsFieldToDisable: true,
		margin: '0 0 5 0',

		plugins: Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGridEditorPlugin'),

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
						tooltip: CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove,
						handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
							me.store.remove(record);
						}
					}
				]
			}
		],

		store: Ext.create('Ext.data.Store', {
			model: 'CMDBuild.model.CMModelTasks.common.workflowForm',
			data: []
		}),

		tbar: [
			{
				text: CMDBuild.Translation.common.buttons.add,
				iconCls: 'add',
				handler: function() {
					me.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.common.workflowForm'));
				}
			}
		]
	});

})();