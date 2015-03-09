(function() {

	Ext.define('CMDBuild.core.proxy.Classes', {
		alternateClassName: 'CMDBuild.ServiceProxy.classes', // Legacy class name

		requires: [
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'GET',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.read,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: true,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		save: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.update,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: true,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: true,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();