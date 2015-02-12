(function() {

	var tr = CMDBuild.Translation.administration.email.templates.valuesWindow;

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplatesVariablesWindow', {
		extend: 'CMDBuild.PopupWindow',

		/**
		 * @cfg {CMDBuild.controller.administration.email.CMEmailTemplatesController}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		title: tr.title,
		buttonsAlign: 'center',

		initComponent: function() {
			var me = this;

			this.grid = Ext.create('Ext.grid.Panel', {
				border: false,
				frame: false,

				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.KEY,
						text: CMDBuild.Translation.key,
						flex: 1,

						editor: { xtype: 'textfield' }
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						text: CMDBuild.Translation.value,
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
									grid.getStore().remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.EmailTemplates.variablesWindow',
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
									me.grid.getStore().insert(0, Ext.create('CMDBuild.model.EmailTemplates.variablesWindow'));
								}
							}
						]
					}
				]
			});

			Ext.apply(this, {
				items: [this.grid],
				buttons: [
					Ext.create('CMDBuild.buttons.ConfirmButton', {
						handler: function() {
							me.delegate.cmOn('onVariablesWindowSave');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						handler: function() {
							me.delegate.cmOn('onVariablesWindowAbort');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();