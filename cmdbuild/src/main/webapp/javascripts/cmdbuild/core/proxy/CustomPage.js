(function() {

	Ext.define('CMDBuild.core.proxy.CustomPage', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		readForCurrentUser: function(parameters) {
			CMDBuild.core.Ajax.request({
				url: CMDBuild.core.proxy.Index.customPage.readForCurrentUser,
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