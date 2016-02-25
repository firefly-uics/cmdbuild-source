(function() {

	Ext.define('CMDBuild.core.proxy.session.Rest', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		login: function (parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				jsonData: parameters.params,
				url: CMDBuild.core.proxy.Index.session.rest + '/',
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
		logout: function (parameters) {
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.interfaces.Ajax.request({
				method: 'DELETE',
				headers: parameters.headers,
				url: CMDBuild.core.proxy.Index.session.rest + '/' + parameters.urlParams[CMDBuild.core.constants.Proxy.TOKEN],
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
		read: function (parameters) {
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.interfaces.Ajax.request({
				method: 'GET',
				headers: parameters.headers,
				jsonData: parameters.params,
				url: CMDBuild.core.proxy.Index.session.rest + '/' + parameters.urlParams[CMDBuild.core.constants.Proxy.TOKEN],
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();
