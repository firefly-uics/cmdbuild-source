(function() {

	Ext.define('CMDBuild.core.proxy.widgets.OpenReport', {

		requires: [
			'CMDBuild.core.proxy.Constants',
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
				fields: [CMDBuild.core.proxy.Constants.VALUE, CMDBuild.core.proxy.Constants.DESCRIPTION],
				data: [
					[CMDBuild.core.proxy.Constants.PDF, CMDBuild.Translation.pdf],
					[CMDBuild.core.proxy.Constants.CSV, CMDBuild.Translation.csv],
					[CMDBuild.core.proxy.Constants.ODT, CMDBuild.Translation.odt],
					[CMDBuild.core.proxy.Constants.RTF, CMDBuild.Translation.rtf]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
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
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
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