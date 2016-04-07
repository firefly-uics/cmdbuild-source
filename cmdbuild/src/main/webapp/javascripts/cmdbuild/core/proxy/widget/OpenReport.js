(function() {

	Ext.define('CMDBuild.core.proxy.widget.OpenReport', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.widget.openReport.ReportCombo'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @management
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.Index.report.createReportFactory
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @management
		 */
		createFactory: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.Index.report.createReportFactoryByTypeCode
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 *
		 * @management
		 */
		getStoreFormats: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.PDF, CMDBuild.Translation.pdf],
					[CMDBuild.core.constants.Proxy.CSV, CMDBuild.Translation.csv],
					[CMDBuild.core.constants.Proxy.ODT, CMDBuild.Translation.odt],
					[CMDBuild.core.constants.Proxy.RTF, CMDBuild.Translation.rtf]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 *
		 * @administration
		 */
		getStoreForceFormat: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.PDF, CMDBuild.Translation.pdf],
					[CMDBuild.core.constants.Proxy.CSV, CMDBuild.Translation.csv]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @administration
		 */
		getStoreReports: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.REPORT, {
				autoLoad: true,
				model: 'CMDBuild.model.widget.openReport.ReportCombo',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.report.getReportsByType,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: {
						type: CMDBuild.core.constants.Proxy.CUSTOM
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
		 * @management
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.Index.report.updateReportFactoryParams
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		}
	});

})();
