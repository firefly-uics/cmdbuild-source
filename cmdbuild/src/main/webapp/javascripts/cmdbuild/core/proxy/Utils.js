(function() {

	Ext.define('CMDBuild.core.proxy.Utils', {

		requires: ['CMDBuild.core.proxy.Index'],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		clearCache: function(parameters) {
			CMDBuild.Ajax.request( {
				url: CMDBuild.core.proxy.Index.utils.clearCache,
				params: parameters.params,
				scope: parameters.scope,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
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
				url: CMDBuild.core.proxy.Index.utils.generateId,
				params: parameters.params,
				scope: parameters.scope,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();