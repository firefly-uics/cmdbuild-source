(function() {

	Ext.define('CMDBuild.controller.management.report.Report', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report',
			'CMDBuild.model.Report'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'createReport',
			'managedReportGet',
			'onReportGenerateButtonClick',
			'showReport'
		],

		/**
		 * Witch report types will be viewed inside modal pop-up
		 *
		 * @cfg {Array}
		 */
		forceDownloadTypes: [
			CMDBuild.core.proxy.CMProxyConstants.ODT,
			CMDBuild.core.proxy.CMProxyConstants.RTF
		],

		/**
		 * @property {CMDBuild.view.management.report.GridPanel}
		 */
		grid: undefined,

		/**
		 * Parameters of last managed report
		 *
		 * @cfg {CMDBuild.model.Report.createParameters}
		 */
		managedReport: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedReportTypes: [
			CMDBuild.core.proxy.CMProxyConstants.CSV,
			CMDBuild.core.proxy.CMProxyConstants.ODT,
			CMDBuild.core.proxy.CMProxyConstants.PDF,
			CMDBuild.core.proxy.CMProxyConstants.RTF
		],

		/**
		 * @cfg {CMDBuild.view.management.report.ReportView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.management.report.ReportView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.grid = Ext.create('CMDBuild.view.management.report.GridPanel', {
				delegate: this
			});

			this.view.add(this.grid);
		},

		/**
		 * @param {Object} parameters
		 */
		createReport: function(parameters) {
			if (Ext.isObject(parameters) && !Ext.isEmpty(parameters[CMDBuild.core.proxy.CMProxyConstants.ID])) {
				this.managedReportSet(parameters);

				CMDBuild.core.proxy.Report.createReport({
					params: this.managedReport.getData(),
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(
							CMDBuild.Translation.error,
							CMDBuild.Translation.errors.createReportFilure,
							false
						);
					},
					success: function(response, options, decodedResponse) {
						if(decodedResponse.filled) { // Report with no parameters
							this.showReport();
						} else { // Show parameters window
							Ext.create('CMDBuild.controller.management.report.Parameters', {
								parentDelegate: this,
								attributeList: decodedResponse.attribute,
								forceDownload: this.managedReport.get(CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD)
							});
						}
					}
				});
			}
		},

		// Managed report property methods
			/**
			 * @return {Object} managedReport
			 */
			managedReportGet: function() {
				return this.managedReport;
			},

			/**
			 * @param {Object} parameters
			 */
			managedReportSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					this.managedReport = Ext.create('CMDBuild.model.Report.createParameters', parameters);
				} else {
					this.managedReport = null;
				}
			},

		/**
		 * @param {Object} reportInfo
		 */
		onReportGenerateButtonClick: function(reportInfo) {
			if (Ext.Array.contains(this.supportedReportTypes, reportInfo[CMDBuild.core.proxy.CMProxyConstants.EXTENSION])) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = reportInfo[CMDBuild.core.proxy.CMProxyConstants.RECORD].get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION);
				params[CMDBuild.core.proxy.CMProxyConstants.ID] = reportInfo[CMDBuild.core.proxy.CMProxyConstants.RECORD].get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.EXTENSION] = reportInfo[CMDBuild.core.proxy.CMProxyConstants.EXTENSION];
				params[CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD] = Ext.Array.contains(this.forceDownloadTypes, reportInfo[CMDBuild.core.proxy.CMProxyConstants.EXTENSION]); // Force download true for PDF and CSV

				this.createReport(params);
			} else {
				CMDBuild.Msg.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.errors.unmanagedReportType,
					false
				);
			}
		},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.grid.getStore().load();

				if (
					!Ext.isEmpty(node.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					&& node.get(CMDBuild.core.proxy.CMProxyConstants.ID) != CMDBuild.core.proxy.CMProxyConstants.CUSTOM
				) {
					this.createReport({
						id: node.get(CMDBuild.core.proxy.CMProxyConstants.ID),
						extension: node.get(CMDBuild.core.proxy.CMProxyConstants.TYPE).replace(/report/i, '') // Removes 'report' string from type property in node object
					});
				}

				this.callParent(arguments);
			}
		},

		/**
		 * Get created report from server and display it in popup window
		 */
		showReport: function() {
			if (!Ext.Object.isEmpty(this.managedReport))
				if (this.managedReport.get(CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD)) { // Force download mode
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD_PARAM_KEY] = true;

					var form = Ext.create('Ext.form.Panel', {
						standardSubmit: true,
						url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory
					});

					form.submit({
						target: '_blank',
						params: params
					});

					Ext.defer(function() { // Form cleanup
						form.close();
					}, 100);
				} else { // Pop-up display mode
					Ext.create('CMDBuild.controller.management.report.Modal', {
						parentDelegate: this,
						format: this.managedReport.get(CMDBuild.core.proxy.CMProxyConstants.EXTENSION)
					});
				}
		}
	});

})();