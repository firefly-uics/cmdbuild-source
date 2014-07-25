(function() {

	var tr = CMDBuild.Translation.administration.email.templates.valuesWindow; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplatesVariablesWindow', {
		extend: 'CMDBuild.PopupWindow',

		delegate: undefined,
		title: tr.title,

		initComponent: function() {
			var me = this;

			this.grid = Ext.create('Ext.grid.Panel', {
				border: false,
				frame: false,

				columns: [
					{
						header: CMDBuild.Translation.key,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.KEY,
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
					model: 'CMDBuild.model.CMModelEmailTemplates.variablesWindow',
					data: []
				}),

				plugins: [
					Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1
					})
				],

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
									me.grid.store.insert(0, Ext.create('CMDBuild.model.CMModelEmailTemplates.variablesWindow'));
								}
							}
						]
					}
				]
			});

			this.fbar = [
				{
					xtype: 'tbspacer',
					flex: 1
				},
				Ext.create('CMDBuild.buttons.ConfirmButton', {
					handler: function() {
						me.delegate.cmOn('onVariablesWindowSave');
					}
				}),
				Ext.create('CMDBuild.buttons.AbortButton', {
					handler: function() {
						me.delegate.cmOn('onVariablesWindowAbort');
					}
				}),
				{
					xtype: 'tbspacer',
					flex: 1
				}
			];

			Ext.apply(this, {
				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();