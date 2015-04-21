(function() {

	var tr = CMDBuild.Translation.administration.email.accounts;

	Ext.define('CMDBuild.view.administration.email.CMEmailAccountsGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			this.gridColumns = [
				{
					text: tr.isDefault,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT,
					align: 'center',
					width: 50,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					scope: this,
					renderer: this.defaultGridColumnRenderer
				},
				{
					text: CMDBuild.Translation.name,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.NAME,
					flex: 1
				},
				{
					text: CMDBuild.Translation.address,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.ADDRESS,
					flex: 1
				}
			];

			this.gridStore = CMDBuild.core.proxy.CMProxyEmailAccounts.getStore();

			Ext.apply(this, {
				columns: this.gridColumns,
				store: this.gridStore
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected');
			},

			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function() {
				this.store.load({
					scope: this,
					callback: function() {
						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		},

		/**
		 * Default column renderer to add icon in grid
		 *
		 * @param {Boolean} value
		 */
		defaultGridColumnRenderer: function(value) {
			return value ? '<img src="images/icons/tick.png" alt="' + tr.isDefault + '" />' : null;
		}
	});

})();