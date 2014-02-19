(function() {

	Ext.define('CMDBuild.ServiceProxy.configuration.email.accounts', {
		statics: {
			create: function(parameters) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.configuration.email.accounts.post,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			get: function() {
				return Ext.create('Ext.data.JsonStore', {
					autoLoad: false,
					model: 'CMDBuild.model.configuration.email.accounts.singleAccount',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.configuration.email.accounts.get,
						reader: {
							type: 'json',
							root: 'response'
						}
					}
				});
			},

			getStore: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.configuration.email.accounts.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.configuration.email.accounts.getStore,
						reader: {
							type: 'json',
							root: 'response.elements'
						}
					},
					sorters: [{
						property: 'address',
						direction: 'ASC'
					}]
				});
			},

			// TODO: to implement for dynamic columns object build with ExtJs grid column configuration
			getStoreColumns: function() {},

			remove: function(params) {
				params.method = 'POST';
				params.url = CMDBuild.ServiceProxy.url.configuration.email.accounts.delete;

				CMDBuild.ServiceProxy.core.doRequest(params);
			},

			update: function(parameters) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.configuration.email.accounts.put,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
		}
	});

})();