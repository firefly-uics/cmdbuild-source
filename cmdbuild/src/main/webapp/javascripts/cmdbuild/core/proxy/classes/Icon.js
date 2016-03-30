(function () {

	/**
	 * REST proxy
	 *
	 * FIXME: future refactor for a correct implementation
	 */
	Ext.define('CMDBuild.core.proxy.classes.Icon', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		createImage: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: 'services/json/file/upload?'	+ CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY + '=' + Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN) // Headers not supported in form submit
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function (parameters) {
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.interfaces.Ajax.request({
				method: 'DELETE',
				headers: parameters.headers,
				jsonData: parameters.params,
				url: 'services/rest/v2/icons/' + parameters.urlParams['iconId'],
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				success: function (response, options, decodedResponse) {
					CMDBuild.core.interfaces.Ajax.request({
						method: 'DELETE',
						headers: parameters.headers,
						jsonData: parameters.params,
						url: 'services/rest/v2/filestores/images/folders/' + parameters.urlParams['folderId'] + '/files/' + parameters.urlParams['imageId'] + '/',
						loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
						scope: parameters.scope || this,
						success: parameters.success || Ext.emptyFn,
						failure: parameters.failure || Ext.emptyFn,
						callback: parameters.callback || Ext.emptyFn
					});
				}
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getFolders: function (parameters) {
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.interfaces.Ajax.request({
				method: 'GET',
				headers: parameters.headers,
				jsonData: parameters.params,
				url: 'services/rest/v2/filestores/images/folders/',
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
		readAllIcons: function (parameters) {
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.interfaces.Ajax.request({
				method: 'GET',
				headers: parameters.headers,
				jsonData: parameters.params,
				url: 'services/rest/v2/icons/',
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
		update: function (parameters) {
			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				params: parameters.params,
				headers: parameters.headers,
				jsonData: parameters.params,
				url: 'services/rest/v2/icons/',
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();
