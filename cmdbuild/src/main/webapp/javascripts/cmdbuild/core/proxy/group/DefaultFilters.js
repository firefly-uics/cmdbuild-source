(function() {

	Ext.define('CMDBuild.core.proxy.group.DefaultFilters', {

		requires: [
			'CMDBuild.core.Cache',
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
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.defaultFilters.read
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAllGroupFilters: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.defaultFilters.readAllGroupFilters
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.defaultFilters.update
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();