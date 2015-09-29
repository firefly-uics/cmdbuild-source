(function() {

	Ext.define('CMDBuild.core.proxy.CustomPage', {

		requires: ['CMDBuild.core.proxy.CMProxyUrlIndex'],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		readForCurrentUser: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.customPage.readForCurrentUser,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.customPage.readAll,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();