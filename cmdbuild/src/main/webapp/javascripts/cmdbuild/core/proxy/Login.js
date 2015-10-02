(function() {

	Ext.define('CMDBuild.core.proxy.Login', {

		requires: ['CMDBuild.core.proxy.Index'],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		doLogin: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.login,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();