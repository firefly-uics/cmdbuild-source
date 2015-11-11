(function() {

	Ext.define('CMDBuild.core.proxy.dataView.DataView', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * Read all the data view available for the logged user
		 *
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.Index.dataView.readAll,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();