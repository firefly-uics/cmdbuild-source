(function() {

	Ext.define('CMDBuild.core.proxy.dataView.Filter', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.dataView.Filter'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				url: CMDBuild.core.proxy.Index.dataView.filter.create,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.dataView.Filter',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.dataView.filter.readAll,
					reader: {
						type: 'json',
						root: 'views'
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * TODO: waiting for refactor (crud)
		 */
		read: function(parameters) {},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				url: CMDBuild.core.proxy.Index.dataView.filter.remove,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				url: CMDBuild.core.proxy.Index.dataView.filter.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();