(function() {

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
			CMDBuild.Ajax.request({
				method: 'POST',
				jsonData: parameters.params,
				url: CMDBuild.core.proxy.CMProxyUrlIndex.session.rest.login,
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
			CMDBuild.Ajax.request({
				method: 'DELETE',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.session.rest.logout + '/' + parameters.params[CMDBuild.core.proxy.CMProxyConstants.TOKEN],
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},
	});

})();