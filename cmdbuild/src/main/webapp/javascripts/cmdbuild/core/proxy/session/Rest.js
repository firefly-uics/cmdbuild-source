(function() {

	/**
	 * Uses Ext.Ajax class to avoid errors on empty response decode and to display 404 errors when trying to get inexistent session
	 */
	Ext.define('CMDBuild.core.proxy.session.Rest', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		login: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				jsonData: parameters.params,
				url: CMDBuild.core.proxy.CMProxyUrlIndex.session.rest + '/',
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
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.proxy.CMProxyConstants.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.proxy.CMProxyConstants.SESSION_TOKEN);

			CMDBuild.Ajax.request({
				method: 'DELETE',
				headers: parameters.headers,
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
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.proxy.CMProxyConstants.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.proxy.CMProxyConstants.SESSION_TOKEN);

			CMDBuild.Ajax.request({
				method: 'GET',
				headers: parameters.headers,
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