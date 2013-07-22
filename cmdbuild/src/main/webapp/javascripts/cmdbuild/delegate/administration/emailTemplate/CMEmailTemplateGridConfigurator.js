Ext.define("CMDBuild.delegate.administration.emailTemplate.CMEmailTemplateGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		if (this.store == null) {
			this.store = _CMProxy.emailTemplate.store();
		}

		return this.store;
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = [ //
			{
				dataIndex: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME,
				header: "@@ Name",
				flex: 1
			},{
				dataIndex:  CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				header: "@@ Description",
				flex: 2
			},{
				dataIndex:  CMDBuild.ServiceProxy.parameter.SUBJECT,
				header: "@@ Subject",
				flex: 3
			}
		];

		return columns;
	}
});