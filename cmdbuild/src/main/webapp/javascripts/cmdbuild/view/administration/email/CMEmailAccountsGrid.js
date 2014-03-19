(function() {

	var tr = CMDBuild.Translation.administration.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailAccountsGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				columns: [
					{
						text: tr.isDefault,
						dataIndex: CMDBuild.ServiceProxy.parameter.IS_DEFAULT,
						align: 'center',
						width: 60,
						renderer: me.defaultGridColumnRenderer
					},
					{
						text: CMDBuild.Translation.name,
						dataIndex: CMDBuild.ServiceProxy.parameter.NAME,
						flex: 1
					},
					{
						text: tr.address,
						dataIndex: CMDBuild.ServiceProxy.parameter.ADDRESS,
						flex: 1
					}
				],
				store: CMDBuild.core.serviceProxy.CMProxyEmailAccounts.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected', {
					'row': row,
					'record': record,
					'index': index
				});
			},

			/**
			 * Event to load store on view display and first row selection as CMDbuild standard
			 */
			viewready: function() {
				var me = this;

				this.store.load({
					callback: function() {
						if (!me.getSelectionModel().hasSelection())
							me.getSelectionModel().select(0, true);
					}
				});
			}
		},

		/**
		 * isDefault renderer to add icon in grid
		 *
		 * @param (Object) value
		 */
		defaultGridColumnRenderer: function(value) {
			if(typeof value == 'boolean') {
				if(value) {
					value = '<img src="images/icons/tick.png" alt="Is Default" />';
				} else {
					value = null;
				}
			}

			return value;
		}
	});

})();