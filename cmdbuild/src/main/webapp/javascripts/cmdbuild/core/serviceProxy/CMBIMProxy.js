CMDBuild.ServiceProxy.bim = {

	enable: function(config) {
		config.method = "POST";
		config.url = CMDBuild.ServiceProxy.url.bim.enable;

		CMDBuild.ServiceProxy.core.doRequest(config);
	},

	disable: function(config) {
		config.method = "POST";
		config.url = CMDBuild.ServiceProxy.url.bim.disable;

		CMDBuild.ServiceProxy.core.doRequest(config);
	},

	store: function() {

		var store = Ext.create("Ext.data.Store", {
			model: "CMDBuild.model.CMBIMProjectModel",
			// fields: ["name", "description", "active"],
			proxy: {
				type: 'ajax',
				url : CMDBuild.ServiceProxy.url.bim.read,
				actionMethods: "GET",
				reader: {
					type: 'json',
					root: 'bimProjects'
				}
			},
			autoLoad: false,

			sorters: [{
				property: "description",
				direction: "ASC"
			}],

			// Disable paging
			defaultPageSize: 0,
			pageSize: 0 
		});

		return store;
	}
};