(function() {

	Ext.define('CMDBuild.ServiceProxy.configuration.email.templates', {
		statics: {
			create: function(parameters) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.configuration.email.templates.post,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			get: function() {
				return Ext.create('Ext.data.JsonStore', {
					autoLoad: false,
					model: 'CMDBuild.model.configuration.email.templates.singleTemplate',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.configuration.email.templates.get,
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
					model: 'CMDBuild.model.configuration.email.templates.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.configuration.email.templates.getStore,
						reader: {
							type: 'json',
							root: 'response.elements'
						}
					},
					sorters: [{
						property: 'name',
						direction: 'ASC'
					}]
				});
			},

			// TODO: to implement for dynamic columns object build with ExtJs grid column configuration
			getStoreColumns: function() {},

			remove: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.configuration.email.templates.delete,
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
					url: CMDBuild.ServiceProxy.url.configuration.email.templates.put,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
		}
	});

})();