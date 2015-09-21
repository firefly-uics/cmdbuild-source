(function() {

	Ext.define('CMDBuild.core.proxy.group.DefaultFilters', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.group.defaultFilters.Filter'
		],

		singleton: true,


		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.data.Store}
		 */
		getClassFiltersStore: function(parameters) {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.group.defaultFilters.Filter',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.group.defaultFilters.readAllGroupFilters,
					reader: {
						type: 'json',
						root: 'filters'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.group.defaultFilters.read,
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
		readAllGroupFilters: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.group.defaultFilters.readAllGroupFilters,
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
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.group.defaultFilters.update,
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