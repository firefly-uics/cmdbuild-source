(function() {

	Ext.define('CMDBuild.core.proxy.PatchManager', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.patchManager.Patch'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.patchManager.Patch',
				remoteSort: false,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.patchManager.readAll,
					reader: {
						type: 'json',
						root: 'patches'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				}
			});
		},

		/**
		 * Apply all new patches
		 *
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				url: CMDBuild.core.proxy.Index.patchManager.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				timeout: CMDBuild.core.configurations.Timeout.getPatchManager(), // Get report timeout from configuration
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();