(function() {

	Ext.define('CMDBuild.core.proxy.widgets.OpenReport', {
		requires: ['CMDBuild.model.widget.CMModelOpenReport'],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		getReportAttributes: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.openReport.createReportFactory,
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
					url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.openReport.getReportsByType,
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