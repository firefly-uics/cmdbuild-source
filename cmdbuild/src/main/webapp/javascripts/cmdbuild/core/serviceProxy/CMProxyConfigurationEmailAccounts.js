(function() {

	Ext.require('CMDBuild.model.CMModelConfigurationEmailAccounts');

	Ext.define('CMDBuild.core.serviceProxy.CMProxyConfigurationEmailAccounts', {
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
					model: 'CMDBuild.model.CMModelConfigurationEmailAccounts.singleAccount',
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
					model: 'CMDBuild.model.CMModelConfigurationEmailAccounts.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.configuration.email.accounts.getStore,
						reader: {
							type: 'json',
							root: 'response.elements'
						}
					},
					sorters: [{
						property: CMDBuild.ServiceProxy.parameter.NAME,
						direction: 'ASC'
					}]
				});
			},

			remove: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.configuration.email.accounts.delete,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			setDefault: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.configuration.email.accounts.setDefault,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
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