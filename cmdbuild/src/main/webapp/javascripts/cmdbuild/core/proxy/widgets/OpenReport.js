(function() {

	Ext.define('CMDBuild.core.proxy.widgets.OpenReport', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.widget.CMModelOpenReport'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		generateReport: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.updateReportFactoryParams,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getReportAttributes: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.createReportFactory,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getReportParameters: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.createReportFactoryByTypeCode,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				timeout: CMDBuild.core.configurations.Timeout.getReport(), // Get report timeout from configuration
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
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