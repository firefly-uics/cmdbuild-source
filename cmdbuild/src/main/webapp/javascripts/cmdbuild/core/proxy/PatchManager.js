(function() {

	Ext.define('CMDBuild.core.proxy.PatchManager', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.patchManager.Patch'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				model: 'CMDBuild.model.patchManager.Patch',
				remoteSort: false,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.patchManager.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.PATCHES
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
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getPatchManager(), // Get patch timeout from configuration
				url: CMDBuild.core.proxy.Index.patchManager.update
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		}
	});

})();