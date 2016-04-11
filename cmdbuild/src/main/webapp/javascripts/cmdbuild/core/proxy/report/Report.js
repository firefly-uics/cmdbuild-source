(function () {

	/**
	 * @management
	 */
	Ext.define('CMDBuild.core.proxy.report.Report', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.report.Grid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.core.proxy.index.Json.report.createReportFactory
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.REPORT, {
				autoLoad: false,
				model: 'CMDBuild.model.report.Grid',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.report.getReportsByType,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 *
		 * @management
		 */
		getTypesTree: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.report.getReportTypesTree });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.index.Json.report.updateReportFactoryParams
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters, true);
		}
	});

})();
