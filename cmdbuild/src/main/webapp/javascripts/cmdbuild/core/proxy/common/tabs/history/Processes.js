(function () {

	Ext.define('CMDBuild.core.proxy.common.tabs.history.Processes', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.workflow.tabs.history.CardRecord'
		],

		singleton: true,

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		get: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.loadMask = Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false; // FIXME

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.classes.getCardHistory }); // TODO: waiting for refactor (different endpoint)

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		getHistoric: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.workflow.getHistoricWorkflow });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		getRelations: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.loadMask = Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false; // FIXME

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.workflow.getRelationsHistory });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		getRelationHistoric: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.workflow.getHistoricRelation });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.HISTORY, {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.tabs.history.CardRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.history.workflow.getWorkflowHistory,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [ // Setup sorters, also if server returns ordered collection
					{ property: CMDBuild.core.constants.Proxy.BEGIN_DATE, direction: 'DESC' }
				]
			});
		}
	});

})();
