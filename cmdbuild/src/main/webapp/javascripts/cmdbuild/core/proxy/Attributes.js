(function() {

	Ext.define('CMDBuild.core.proxy.Attributes', {
		alternateClassName: 'CMDBuild.ServiceProxy.attributes', // Legacy class name

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.Index.attribute.read,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.attribute.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		reorder: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.attribute.reorder,
				params: parameters.params,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.attribute.update,
				params: parameters.params,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		updateSortConfiguration: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.attribute.updateSortConfiguration,
				params: parameters.params,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();