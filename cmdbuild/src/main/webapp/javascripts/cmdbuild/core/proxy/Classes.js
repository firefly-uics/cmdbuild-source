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
			CMDBuild.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.read,
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
		save: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.remove,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();