Ext.define("CMDBuild.delegate.administration.bim.CMBIMGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		if (this.store == null) {
			this.store = CMDBuild.bim.proxy.store();
		}

		return this.store;
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = this.callParent(arguments);

		columns.push({
			dataIndex: "active",
			header: CMDBuild.Translation.active,
			flex: 1
		}, {
			dataIndex: "lastCheckin",
			header: CMDBuild.Translation.last_checkin,
			flex: 1
		});

		return columns;
	}
});