(function() {

	Ext.define('CMDBuild.controller.management.report.SingleReport', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportDownloadButtonClick',
			'onReportTypeButtonClick'
		],

		/**
		 * @property {Object}
		 */
		displayedReportParams: undefined,

		/**
		 * @cfg {CMDBuild.view.management.report.SingleReportPanel}
		 */
		view: undefined,

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
						CMDBuild.Msg.error(
							CMDBuild.Translation.error,
							CMDBuild.Translation.errors.createReportFilure,
							false
						);
					},
					success: function(response, options, decodedResponse) {
						this.displayedReportParams = reportParams;

						if(decodedResponse.filled) { // Report with no parameters
							this.showReport(forceDownload);
						} else { // Show parameters window
							if (Ext.isIE) // FIX: in IE PDF is painted on top of the regular page content so remove it before display parameter window
								this.view.removeAll();

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

		onReportDownloadButtonClick: function() {
			if (!Ext.Object.isEmpty(this.displayedReportParams))
				this.createReport(this.displayedReportParams, true);
		},

		/**
		 * @param {String} type
		 */
		onReportTypeButtonClick: function(type) {
			if (Ext.Array.contains(this.supportedReportTypes, type)) {
				this.createReport({
					id: this.reportId,
					extension: type
				});
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
			if (
				!Ext.Object.isEmpty(node)
				&& !Ext.isEmpty(node.get(CMDBuild.core.proxy.CMProxyConstants.ID))
				&& node.get(CMDBuild.core.proxy.CMProxyConstants.ID) != CMDBuild.core.proxy.CMProxyConstants.CUSTOM
			) {
				this.view.setTitle(this.view.sectionTitle + ' - ' + node.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				this.reportId = node.get(CMDBuild.core.proxy.CMProxyConstants.ID);

				this.createReport({
					id: node.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					extension: node.get(CMDBuild.core.proxy.CMProxyConstants.TYPE).replace(/report/i, '') // Removes 'report' string from type property in node object
				});

				this.callParent(arguments);
			}
		},

		/**
		 * Get created report from server and display it in iframe
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
			} else { // Add to view display mode
				this.view.removeAll();

				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory
					}
				});
			}
		}
	});

})();
