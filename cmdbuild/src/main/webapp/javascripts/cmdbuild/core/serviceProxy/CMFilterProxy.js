(function() {

	var url = _CMProxy.url.filter;
	var GET = "GET";
	var POST = "POST";

	CMDBuild.ServiceProxy.Filter = {

		create: function(filter, config) {
			var fullParams = true;
			doRequest(filter, config, url.create, POST, fullParams);
		},

		update: function(filter, config) {
			var fullParams = true;
			doRequest(filter, config, url.update, POST, fullParams);
		},

		remove: function(filter, config) {
			var fullParams = false;
			doRequest(filter, config, url.remove, POST. fullParams);
		},

		position: function(filter, config) {
			var fullParams = false;
			doRequest(filter, config, url.position, GET, fullParams);
		},

		newUserStore: function() {
			return new Ext.data.Store({
				model: "CMDBuild.model.CMFilterModel",
				autoLoad: false,
				proxy: {
					type: "ajax",
					url: url.userStore,
					reader: {
						type: 'json',
						root: 'filters'
					}
				}
			 });
		},

		newSystemStore: function() {
			return new Ext.data.Store({
				fields: ["name", "description"],
				pageSize: _CMUtils.grid.getPageSize(),
				proxy: {
					url: _CMProxy.url.filter.read,
					type: "ajax",
					reader: {
						root: "filters",
						type: "json",
						totalProperty: "count"
					}
				},
				autoLoad: true
			});
		}
	};

	function doRequest(filter, config, url, method, fullParams) {
		if (Ext.getClassName(filter) != "CMDBuild.model.CMFilterModel") {
			return; // TODO alert
		}

		var request = config || {};

		request.url = url;
		request.method = method;
		request.params = getParams(filter, fullParams);

		CMDBuild.Ajax.request(config);
	}

	function getParams(filter, full) {
		var params = {};

		params.id = filter.getId();
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