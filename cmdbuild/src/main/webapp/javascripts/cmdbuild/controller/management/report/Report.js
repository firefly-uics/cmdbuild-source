(function() {

	Ext.define('CMDBuild.controller.management.report.Report', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: ['onReportGenerateButtonClick'],

		/**
		 * @property {CMDBuild.view.management.report.GridPanel}
		 */
		grid: undefined,

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
		 * @param {Object} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.grid = Ext.create('CMDBuild.view.management.report.GridPanel', {
				delegate: this
			});

			this.view.add(this.grid);
		},

		/**
		 * @param {Object} reportParams
		 * @param {Boolean} forceDownload
		 */
		createReport: function(reportParams, forceDownload) {
			forceDownload = forceDownload || false;

			if (!Ext.isEmpty(reportParams[CMDBuild.core.proxy.CMProxyConstants.ID])) {
				reportParams[CMDBuild.core.proxy.CMProxyConstants.TYPE] = reportParams[CMDBuild.core.proxy.CMProxyConstants.TYPE] || 'CUSTOM';
				reportParams[CMDBuild.core.proxy.CMProxyConstants.EXTENSION] = reportParams[CMDBuild.core.proxy.CMProxyConstants.EXTENSION] || CMDBuild.core.proxy.CMProxyConstants.PDF;

				CMDBuild.core.proxy.Report.createReport({
					scope: this,
					params: reportParams,
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.error,
							CMDBuild.Translation.errors.createReportFilure,
							false
						);
					},
					success: function(response, options, decodedResponse) {
						if(decodedResponse.filled) { // Report with no parameters
							this.showReport(forceDownload);
						} else { // Show parameters window
							Ext.create('CMDBuild.controller.management.report.Parameters', {
								parentDelegate: this,
								attributeList: decodedResponse.attribute,
								forceDownload: forceDownload
							});
						}
					}
				});
			}
		},

		/**
		 * @param {Object} reportInfo
		 */
		onReportGenerateButtonClick: function(reportInfo) {
			if (Ext.Array.contains(this.supportedReportTypes, reportInfo[CMDBuild.core.proxy.CMProxyConstants.TYPE])) {
				this.createReport(
					{
						id: reportInfo[CMDBuild.core.proxy.CMProxyConstants.RECORD].get(CMDBuild.core.proxy.CMProxyConstants.ID),
						extension: reportInfo[CMDBuild.core.proxy.CMProxyConstants.TYPE]
					},
					Ext.Array.contains(this.forceDownloadTypes, reportInfo[CMDBuild.core.proxy.CMProxyConstants.TYPE]) // Force download true for PDF and CSV
				);
			} else {
				CMDBuild.core.Message.error(
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
		 *
		 * @param {Boolean} forceDownload
		 */
		showReport: function(forceDownload) {
			forceDownload = forceDownload || false;

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD_PARAM_KEY] = true;

			if (forceDownload) { // Force download mode
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
				var popup = window.open(
					CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory,
					'Report',
					'height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable'
				);

				if (!popup)
					CMDBuild.core.Message.warn(
						CMDBuild.Translation.warnings.warning_message,
						CMDBuild.Translation.warnings.popup_block
					);
			}
		}
	});

})();