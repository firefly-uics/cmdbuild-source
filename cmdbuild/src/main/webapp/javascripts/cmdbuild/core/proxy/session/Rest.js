(function() {

	/**
	 * Uses Ext.Ajax class to avoid errors on empty response decode and to display 404 errors when trying to get inexistent session
	 */
	Ext.define('CMDBuild.core.proxy.session.Rest', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
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
		logout: function(parameters) {
			Ext.Ajax.request({
				method: 'DELETE',
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
		read: function(parameters) {
			Ext.Ajax.request({
				method: 'GET',
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