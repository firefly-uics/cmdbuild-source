(function() {

	Ext.define('CMDBuild.core.proxy.processes.Activity', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		lock: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.processes.instances.lock,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		unlock: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.processes.instances.unlock,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		unlockAll: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.processes.instances.unlockAll,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();