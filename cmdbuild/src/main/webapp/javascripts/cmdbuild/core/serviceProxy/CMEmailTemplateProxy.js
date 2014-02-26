(function() {
	var POST = "POST";
	var GET = "GET";
	var url = _CMProxy.url.emailTemplate;

	_CMProxy.emailTemplate = {

		read: function(config) {
			config.url = url.read;
			config.method = GET;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},


		create: function(config) {
			config.url = url.create;
			config.method = POST;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		update: function(config) {
			config.url = url.update;
			config.method = POST;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		remove: function(config) {
			config.url = url.remove;
			config.method = POST;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		store: function() {
			return new Ext.data.Store({
				model: "CMDBuild.model.CMEmailTemplateModel",
				autoLoad: false,
				proxy: {
					type: "ajax",
					url: url.read,
					reader: {
						type: 'json',
						root: 'templates'
					}
				}
			});
		}
	};
})();