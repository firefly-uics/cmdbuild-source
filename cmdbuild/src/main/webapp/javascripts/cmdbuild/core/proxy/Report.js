(function() {

	Ext.define('CMDBuild.core.proxy.Report', {

		requires: [
			'CMDBuild.core.Utils',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.Report'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		createReport: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.createReportFactory,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				failure: parameters.failure
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getMenuTree: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.menuTree,
				method: 'GET',
				params: parameters.params,
				scope: parameters.scope,
				callback: parameters.callback,
				failure: parameters.failure,
				success: parameters.success
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.Report.grid',
				pageSize: CMDBuild.core.Utils.getPageSize(),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.getReportsByType,
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: {
						type: CMDBuild.core.proxy.CMProxyConstants.CUSTOM
					}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getTypesTree: function(parameters) {
			CMDBuild.ServiceProxy.core.doRequest({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.getReportTypesTree,
				method: 'GET',
				params: parameters.params,
				scope: parameters.scope,
				callback: parameters.callback,
				failure: parameters.failure,
				success: parameters.success
			});
		}

	});

})();