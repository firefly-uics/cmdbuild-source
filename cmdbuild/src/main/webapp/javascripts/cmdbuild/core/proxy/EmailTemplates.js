(function() {

	Ext.define('CMDBuild.core.proxy.EmailTemplates', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.EmailTemplates'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
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
		 * @return {Ext.data.Store} store
		 */
		get: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.EmailTemplates.singleTemplate',
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
		 * @return {Ext.data.Store} store
		 */
		getEmailAccountsStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.CMModelEmailAccounts.grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.getStore,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.CMProxyConstants.NAME,
					direction: 'ASC'
				}]
			});
		},

		/**
		 * @return {Ext.data.Store} store
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.EmailTemplates.grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.getStore,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
					direction: 'ASC'
				}]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.email.templates.remove,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				callback: parameters.callback
			});
		},

		/**
		 * @param {Object} parameters
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
	});

})();