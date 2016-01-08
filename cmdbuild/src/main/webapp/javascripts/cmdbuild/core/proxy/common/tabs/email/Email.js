(function() {

	Ext.define('CMDBuild.core.proxy.common.tabs.email.Email', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.tabs.email.Email'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.post,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.email.Email',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.email.getStore,
					reader: {
						root: 'response',
						type: 'json'
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: {
					property: CMDBuild.core.constants.Proxy.STATUS,
					direction: 'ASC'
				},
				groupField: CMDBuild.core.constants.Proxy.STATUS
			});
		},

		/**
		 * @param {Object} parameters
		 */
		isEmailEnabledForCard: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.enabled,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.put,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();