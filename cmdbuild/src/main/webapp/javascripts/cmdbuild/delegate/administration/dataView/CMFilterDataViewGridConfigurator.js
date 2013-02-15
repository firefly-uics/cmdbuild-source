Ext.define("CMDBuild.delegate.administration.common.dataview.CMFilterDataViewGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		return new Ext.data.Store({
			fields: [
				_CMProxy.parameter.NAME,
				_CMProxy.parameter.DESCRIPTION,
				_CMProxy.parameter.CLASS_NAME,
				_CMProxy.parameter.FILTER
			],
			data: [{
				name: "VistaDaFiltro1",
				description: "Vista Da Viltro 1",
				className: "Asset",
				filter: "Asset prima del 12/09/2002"
			}, {
				name: "VistaDaFiltro2",
				description: "Vista Da Viltro 2",
				className: "Asset",
				filter: "Asset dopo il 12/09/2002"
			}]
		});
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = this.callParent(arguments);
		columns.push({
			dataIndex: _CMProxy.parameter.CLASS_NAME,
			header: CMDBuild.Translation.targetClass,
			flex: 1
		}, {
			dataIndex: _CMProxy.parameter.FILTER,
			header: CMDBuild.Translation.filter,
			flex: 1
		});

		return columns;
	}
});