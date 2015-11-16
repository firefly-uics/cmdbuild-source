(function() {

	Ext.define('CMDBuild.core.proxy.filter.Group', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.filter.group.Store',
			'CMDBuild.model.filter.group.TargetClass'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		getDefaults: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.defaults.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters);
		},

		/**
		 * Returns a store with the filters for a given group
		 *
		 * @return {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FILTER, {
				autoLoad: false,
				model: 'CMDBuild.model.filter.group.Store',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.filter.group.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.FILTERS,
						totalProperty: 'count'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreTargetClass: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.filter.group.TargetClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters root of all classes
						return record.get(CMDBuild.core.constants.Proxy.NAME) != 'Class';
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		setDefaults: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.defaults.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.group.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		}
	});

})();