(function() {

	/**
	 * @management
	 */
	Ext.define('CMDBuild.core.proxy.report.Report', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.report.Grid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.Index.report.createReportFactory
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store}
		 *
		 * FIXME: generalize this implementation building controller function to get type
		 */
		getStore: function() {
			var extraParams = {};
			extraParams[CMDBuild.core.constants.Proxy.TYPE] = 'CUSTOM';

			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.report.Grid',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.report.getReportsByType,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: 'results'
					},
					extraParams: extraParams
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @management
		 */
		getTypesTree: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.report.getReportTypesTree });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.Index.report.updateReportFactoryParams
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters, true);
		}
	});

})();
