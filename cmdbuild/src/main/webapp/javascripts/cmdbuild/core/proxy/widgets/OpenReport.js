(function() {

	Ext.define('CMDBuild.core.proxy.widgets.OpenReport', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.widget.openReport.ReportCombo'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.reports.createReportFactory,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		createFactory: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.reports.createReportFactoryByTypeCode,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getFormatsStore: function() {
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
		 * @return {Ext.data.Store}
		 */
		getReportsStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.widget.openReport.ReportCombo',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.reports.getReportsByType,
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: {
						type: 'custom'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.reports.updateReportFactoryParams,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();