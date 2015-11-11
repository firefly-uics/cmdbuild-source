(function() {

	Ext.define('CMDBuild.core.proxy.Utils', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		clearCache: function(parameters) {
			CMDBuild.core.Ajax.request( {
				url: CMDBuild.core.proxy.Index.utils.clearCache,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		generateId: function(parameters) {
			CMDBuild.core.Ajax.request({
				url: CMDBuild.core.proxy.Index.utils.generateId,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();