(function() {

	Ext.define('CMDBuild.core.proxy.widgets.OpenReport', {
		requires: ['CMDBuild.model.widget.CMModelOpenReport'],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		generateReport: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.updateReportFactoryParams,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				failure: parameters.failure
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getReportAttributes: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.createReportFactory,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getReportParameters: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.createReportFactoryByTypeCode,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getReportsStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.widget.CMModelOpenReport.reportCombo',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.getReportsByType,
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: {
						type: 'custom',
						limit: 1000
					}
				}
			});
		}
	});

})();