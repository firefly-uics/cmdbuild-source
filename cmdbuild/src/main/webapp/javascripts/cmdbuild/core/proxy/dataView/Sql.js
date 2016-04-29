(function() {

	Ext.define('CMDBuild.core.proxy.dataView.Sql', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.Utils',
			'CMDBuild.model.dataView.sql.GridStore',
			'CMDBuild.model.Function'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.sql.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DATA_VIEW, {
				autoLoad: false,
				model: 'CMDBuild.model.dataView.sql.GridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.dataView.sql.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.VIEWS
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
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreDataSources: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FUNCTION, {
				autoLoad: true,
				model: 'CMDBuild.model.Function',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.functions.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
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
		 * @param {Object} parameters.extraParams
		 * @param {Array} parameters.fields
		 *
		 * @return {Ext.data.Store}
		 *
		 * @management
		 */
		getStoreFromSql: function(parameters) {
			parameters = parameters || {};

			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				fields: parameters.fields || [],
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				remoteSort: true,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.card.getSqlCardList,
					reader: {
						type: 'json',
						root: 'cards',
						totalProperty: 'results'
					},
					extraParams: parameters.extraParams || {}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.sql.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.sql.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.sql.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		}
	});

})();
