(function() {

	Ext.define('CMDBuild.view.administration.email.accounts.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.email.Accounts'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Accounts}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.defaultLabel,
						dataIndex: CMDBuild.core.proxy.Constants.IS_DEFAULT,
						align: 'center',
						width: 50,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.defaultLabel + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.proxy.Constants.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.Constants.ADDRESS,
						text: CMDBuild.Translation.address,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.email.Accounts.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onEmailAccountsItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onEmailAccountsRowSelected');
			},

			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function() {
				this.getStore().load({
					scope: this,
					callback: function(records, operation, success) {
						// Store load errors manage
						if (!success) {
							CMDBuild.core.Message.error(null, {
								text: CMDBuild.Translation.errors.unknown_error,
								detail: operation.error
							});
						}

						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		}
	});

})();