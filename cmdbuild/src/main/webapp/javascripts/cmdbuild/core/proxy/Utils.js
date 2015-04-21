(function() {

	Ext.define('CMDBuild.core.proxy.Utils', {

		requires: ['CMDBuild.core.proxy.CMProxyUrlIndex'],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		clearCache: function(parameters) {
			CMDBuild.Ajax.request( {
				url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.clearCache,
				loadMask: true,
				params: parameters.params,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		generateId: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.generateId,
				params: parameters.params,
				scope: parameters.scope,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();