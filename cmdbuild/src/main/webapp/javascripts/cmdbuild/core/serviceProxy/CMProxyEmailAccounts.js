(function() {

	Ext.require('CMDBuild.model.CMModelEmailAccounts');

	Ext.define('CMDBuild.core.serviceProxy.CMProxyEmailAccounts', {
		statics: {
			create: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.email.accounts.post,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			get: function() {
				return Ext.create('Ext.data.JsonStore', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelEmailAccounts.singleAccount',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.email.accounts.get,
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
					model: 'CMDBuild.model.CMModelEmailAccounts.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.email.accounts.getStore,
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
					url: CMDBuild.ServiceProxy.url.email.accounts.delete,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			setDefault: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.email.accounts.setDefault,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			update: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.email.accounts.put,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
		}
	});

})();