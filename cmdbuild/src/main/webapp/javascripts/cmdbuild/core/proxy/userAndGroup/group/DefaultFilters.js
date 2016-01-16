(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.DefaultFilters', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.userAndGroup.group.defaultFilters.Filter'
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
				model: 'CMDBuild.model.userAndGroup.group.defaultFilters.Filter',
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
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.group.defaultFilters.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.group.defaultFilters.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();