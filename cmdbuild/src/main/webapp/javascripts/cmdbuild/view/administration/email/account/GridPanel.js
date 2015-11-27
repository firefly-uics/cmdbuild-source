(function() {

	Ext.define('CMDBuild.view.administration.email.account.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Account}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmborderbottom',
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.ux.grid.column.Tick', {
						text: CMDBuild.Translation.defaultLabel,
						dataIndex: CMDBuild.core.constants.Proxy.IS_DEFAULT,
						align: 'center',
						width: 50,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						iconLabel: CMDBuild.Translation.defaultLabel
					}),
					{
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.ADDRESS,
						text: CMDBuild.Translation.address,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.email.Account.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onEmailAccountItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onEmailAccountRowSelected');
			},

			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function() {
				this.getStore().load({
					scope: this,
					callback: function(records, operation, success) {
						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		}
	});

})();