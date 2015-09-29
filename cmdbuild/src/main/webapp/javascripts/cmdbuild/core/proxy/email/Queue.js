(function() {

	Ext.define('CMDBuild.core.proxy.email.Queue', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		configurationRead: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.email.queue.configuration,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		configurationSave: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.email.queue.configure,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		isRunning: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.email.queue.running,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		start: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.email.queue.start,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		stop: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.email.queue.stop,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();