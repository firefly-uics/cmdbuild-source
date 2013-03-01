Ext.define("CMDBuild.delegate.administration.common.dataview.CMSqlDataViewGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		return new Ext.data.Store({
			fields: [ _CMProxy.parameter.NAME,  _CMProxy.parameter.DESCRIPTION, _CMProxy.parameter.DATASOURCE],
			data: [
				{name: "VistaSQL1", description: "Vista SQL 1", dataSourceName: "cmf_active_asset_for_brand"},
				{name: "VistaSQL2", description: "Vista SQL 2", dataSourceName: "cmf_active_asset_for_brand"}
			]
		});
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = this.callParent(arguments);
		columns.push({
			header: CMDBuild.Translation.administration.modDashboard.charts.fields.dataSource,
			dataIndex: _CMProxy.parameter.DATASOURCE,
			flex: 1
		});

		return columns;
	}
});