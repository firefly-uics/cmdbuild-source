(function() {

	var tr = CMDBuild.Translation.administration.setup.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.configuration.CMConfigurationEmailAccountsGrid', {
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
						dataIndex: 'id',
						hidden: true
					},
					{
						text: tr.isDefault,
						dataIndex: 'isDefault',
						align: 'center',
						width: '60px',
						renderer: me.defaultGridColumnRenderer
					},
					{
						text: tr.name,
						dataIndex: 'name',
						flex: 1
					},
					{
						text: tr.address,
						dataIndex: 'address',
						flex: 1
					}
				],

				// TODO: use a server call to get columns from database
				// columns: CMDBuild.ServiceProxy.configuration.email.getStoreColumns(),

				store: CMDBuild.ServiceProxy.configuration.email.accounts.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected', {
					'row': row,
					'record': record,
					'index': index
				}, null);
			},

			/**
			 * Event to load store on view display and first row selection as CMDbuild standard
			 */
			viewready: function() {
				var me = this;

				this.getStore().load( function() {
					me.getSelectionModel().select(0, true);
				});
			}
		},

		/**
		 * @param {Object} value
		 * Used to render isDefault database value to add icon
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
