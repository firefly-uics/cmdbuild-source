(function() {

	Ext.define('CMDBuild.view.administration.email.templates.VariablesWindow', {
		extend: 'CMDBuild.PopupWindow',

		/**
		 * @cfg {CMDBuild.controller.administration.email.TemplatesController}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		title: CMDBuild.Translation.administration.email.templates.valuesWindow.title,
		buttonsAlign: 'center',

		initComponent: function() {
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
								scope: this,

								handler: function() {
									this.grid.getStore().insert(0, Ext.create('CMDBuild.model.EmailTemplates.variablesWindow'));
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
						scope: this,

						handler: function() {
							this.delegate.cmOn('onVariablesWindowSave');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onVariablesWindowAbort');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();