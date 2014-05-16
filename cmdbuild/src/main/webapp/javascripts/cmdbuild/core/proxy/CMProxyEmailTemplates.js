(function() {

	Ext.require('CMDBuild.model.CMModelEmailTemplates');

	Ext.define('CMDBuild.core.proxy.CMProxyEmailTemplates', {
		statics: {

			/**
			 * @param (Object) parameters
			 */
			create: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.post,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @return (Object) store
			 */
			get: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelEmailTemplates.singleTemplate',
					proxy: {
						type: 'ajax',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.get,
						reader: {
							type: 'json',
							root: 'response'
						}
					}
				});
			},

			/**
			 * @return (Object) store
			 */
			getStore: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelEmailTemplates.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.getStore,
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

			/**
			 * @param (Object) parameters
			 */
			remove: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.delete,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @param (Object) parameters
			 */
			update: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.put,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
		}
	});

})();