(function() {

	Ext.define('CMDBuild.core.proxy.session.JsonRpc', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.Utils'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		login: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				params: parameters.params,
				url: CMDBuild.core.proxy.Index.session.jsonRpc.login,
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
			CMDBuild.core.interfaces.Ajax.request({
				params: parameters.params,
				url: CMDBuild.core.proxy.Index.session.jsonRpc.logout,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();