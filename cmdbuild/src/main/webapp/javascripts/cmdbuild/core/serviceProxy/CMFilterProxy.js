(function() {

	var url = {
		read: "services/json/filter/read",
		create: "services/json/filter/create",
		update: "services/json/filter/update",
		remove: "services/json/filter/delete"
	};

	CMDBuild.ServiceProxy.Filter = {

		read: function(config) {
			// read only the store, and do it directly
		},

		create: function(filter, config) {
			var fullParams = true;
			doRequest(filter, config, url.create, fullParams);
		},

		update: function(filter, config) {
			var fullParams = true;
			doRequest(filter, config, url.update, fullParams);
		},

		remove: function(filter, config) {
			var fullParams = false;
			doRequest(filter, config, url.remove, fullParams);
		}
	};

	function doRequest(filter, config, url, fullParams) {
		if (Ext.getClassName(filter) != "CMDBuild.model.CMFilterModel") {
			return; // TODO alert
		}

		var request = config || {};

		request.url = url;
		request.method = 'POST';
		request.params = getParams(filter, fullParams);

		CMDBuild.Ajax.request(config);
	}

	function getParams(filter, full) {
		var params = {};

		params.name = filter.getName();
		params.className = filter.getEntryType();

		if (full) {
			params.description = filter.getDescription();
			params.configuration = Ext.encode(filter.getConfiguration());
			params.groupName = filter.getGroupName();
		}

		return params;
	}
})();