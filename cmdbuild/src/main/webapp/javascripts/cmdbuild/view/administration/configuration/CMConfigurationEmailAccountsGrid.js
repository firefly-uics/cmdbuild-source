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
				store: CMDBuild.core.serviceProxy.CMProxyConfigurationEmailAccounts.getStore()
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

				this.store.load({
					callback: function() {
						me.getSelectionModel().select(0, true);
					}
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