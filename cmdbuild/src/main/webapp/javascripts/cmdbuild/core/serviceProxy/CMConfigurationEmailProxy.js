(function() {

	CMDBuild.ServiceProxy.configuration.email = {
		createEmailAccount: function(p) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.ServiceProxy.url.configuration.email.post,
				params: p.params,
				scope: p.scope,
				success: p.success,
				callback: p.callback
			});
		},

		get: function() {
			return new Ext.data.JsonStore({
				autoLoad: false,
				model: 'CMDBuild.model.CMConfigurationEmailModel.emailAccount',
				proxy: {
					type: 'ajax',
					url: CMDBuild.ServiceProxy.url.configuration.email.get,
					reader: {
						type: 'json',
						root: 'response'
					}
				}
			});
		},

		getStore: function() {
			return new Ext.data.Store({
				autoLoad: true,
				model: 'CMDBuild.model.CMConfigurationEmailModel.grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.ServiceProxy.url.configuration.email.getStore,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [{
					property: 'address',
					direction: "ASC"
				}]
			});
		},

		// TODO: to implement for dynamic columns object build with ExtJs grid column configuration
		getStoreColumns: function() {},

		removeEmailAccount: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.ServiceProxy.url.configuration.email.delete;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		updateEmailAccount: function(p) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.ServiceProxy.url.configuration.email.put,
				params: p.params,
				scope: p.scope,
				success: p.success,
				callback: p.callback
			});
		}
	};

})();