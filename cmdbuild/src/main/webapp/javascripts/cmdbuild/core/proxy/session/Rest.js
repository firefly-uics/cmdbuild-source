(function() {

	/**
	 * Uses Ext.Ajax class to avoid errors on empty response decode and to display 404 errors when trying to get inexistent session
	 */
	Ext.define('CMDBuild.core.proxy.session.Rest', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.Utils'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		login: function(parameters) {
			Ext.Ajax.request({
				method: 'PUT',
				jsonData: parameters.params,
				url: CMDBuild.core.proxy.CMProxyUrlIndex.session.rest + '/' + parameters.urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN],
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
		logout: function(parameters) {
			Ext.Ajax.request({
				method: 'DELETE',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.session.rest + '/' + parameters.urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN],
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
		read: function(parameters) {
			Ext.Ajax.request({
				method: 'GET',
				jsonData: parameters.params,
				url: CMDBuild.core.proxy.CMProxyUrlIndex.session.rest + '/' + parameters.urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN],
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();