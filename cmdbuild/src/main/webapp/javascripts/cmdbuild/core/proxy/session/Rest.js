//(function () {
//
//	Ext.define('CMDBuild.core.proxy.session.Rest', {
//
//		requires: [
//			'CMDBuild.core.constants.Proxy',
//			'CMDBuild.core.interfaces.Rest',
//			'CMDBuild.core.proxy.index.Json'
//		],
//
//		singleton: true,
//
//		/**
//		 * @param {Object} parameters
//		 */
//		impersonate: function (parameters) {
//			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
//			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//			CMDBuild.core.interfaces.Rest.request({
//				method: 'PUT',
//				headers: parameters.headers,
//				url: CMDBuild.core.proxy.index.Json.session.rest
//					+ '/' + parameters.urlParams[CMDBuild.core.constants.Proxy.TOKEN]
//					+ '/impersonate/' + parameters.urlParams[CMDBuild.core.constants.Proxy.USERNAME],
//				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
//				scope: parameters.scope || this,
//				success: parameters.success || Ext.emptyFn,
//				failure: parameters.failure || Ext.emptyFn,
//				callback: parameters.callback || Ext.emptyFn
//			});
//		},
//
//		/**
//		 * @param {Object} parameters
//		 */
//		login: function (parameters) {
//			CMDBuild.core.interfaces.Rest.request({
//				method: 'POST',
//				jsonData: parameters.params,
//				url: CMDBuild.core.proxy.index.Json.session.rest + '/',
//				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
//				scope: parameters.scope || this,
//				success: parameters.success || Ext.emptyFn,
//				failure: parameters.failure || Ext.emptyFn,
//				callback: parameters.callback || Ext.emptyFn
//			});
//		},
//
//		/**
//		 * @param {Object} parameters
//		 */
//		logout: function (parameters) {
//			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
//			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//			CMDBuild.core.interfaces.Rest.request({
//				method: 'DELETE',
//				headers: parameters.headers,
//				url: CMDBuild.core.proxy.index.Json.session.rest + '/' + parameters.urlParams[CMDBuild.core.constants.Proxy.TOKEN] + '/',
//				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
//				scope: parameters.scope || this,
//				success: parameters.success || Ext.emptyFn,
//				failure: parameters.failure || Ext.emptyFn,
//				callback: parameters.callback || Ext.emptyFn
//			});
//		},
//
//		/**
//		 * @param {Object} parameters
//		 */
//		read: function (parameters) {
//			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
//			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//			CMDBuild.core.interfaces.Rest.request({
//				method: 'GET',
//				headers: parameters.headers,
//				jsonData: parameters.params,
//				url: CMDBuild.core.proxy.index.Json.session.rest + '/' + parameters.urlParams[CMDBuild.core.constants.Proxy.TOKEN] + '/',
//				disableAllMessages: true,
//				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
//				scope: parameters.scope || this,
//				success: parameters.success || Ext.emptyFn,
//				failure: parameters.failure || Ext.emptyFn,
//				callback: parameters.callback || Ext.emptyFn
//			});
//		},
//
//		/**
//		 * @param {Object} parameters
//		 */
//		setGroup: function (parameters) {
//			parameters.headers = Ext.isEmpty(parameters.headers) ? {} : parameters.headers;
//			parameters.headers[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//			CMDBuild.core.interfaces.Rest.request({
//				method: 'PUT',
//				headers: parameters.headers,
//				jsonData: parameters.params,
//				url: CMDBuild.core.proxy.index.Json.session.rest + '/' + parameters.urlParams[CMDBuild.core.constants.Proxy.TOKEN] + '/',
//				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
//				scope: parameters.scope || this,
//				success: parameters.success || Ext.emptyFn,
//				failure: parameters.failure || Ext.emptyFn,
//				callback: parameters.callback || Ext.emptyFn
//			});
//		}
//	});
//
//})();
