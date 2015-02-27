(function() {

	Ext.define('CMDBuild.controller.management.report.Main', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report'
		],

		/**
		 * @property {CMDBuild.view.management.report.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.management.report.MainPanel}
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
		 * @param {Object} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.grid = this.view.grid;
			this.grid.delegate = this;
			this.view.delegate = this;
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onReportGenerateButtonClick' :
					return this.onReportGenerateButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {Object} reportParams
		 */
		createReport: function(reportParams) {
			if (!Ext.isEmpty(reportParams[CMDBuild.core.proxy.CMProxyConstants.ID])) {
				reportParams[CMDBuild.core.proxy.CMProxyConstants.TYPE] = reportParams[CMDBuild.core.proxy.CMProxyConstants.TYPE] || 'CUSTOM';
				reportParams[CMDBuild.core.proxy.CMProxyConstants.EXTENSION] = reportParams[CMDBuild.core.proxy.CMProxyConstants.EXTENSION] || CMDBuild.core.proxy.CMProxyConstants.PDF;

				CMDBuild.LoadMask.get().show();
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
						if(decodedResponse.filled) { // Report with no parameters
							this.showReport();
						} else { // Show parameters window
							Ext.create('CMDBuild.view.management.report.ParametersWindow', {
								delegate: this,
								attributeList: decodedResponse.attribute
							}).show();
						}
					},
					callback: function(options, success, response) {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},

		/**
		 * @param {Object} reportInfo
		 */
		onReportGenerateButtonClick: function(reportInfo) {
			if (Ext.Array.contains(this.supportedReportTypes, reportInfo[CMDBuild.core.proxy.CMProxyConstants.TYPE])) {
				this.createReport({
					id: reportInfo[CMDBuild.core.proxy.CMProxyConstants.RECORD].get(CMDBuild.core.proxy.CMProxyConstants.ID),
					extension: reportInfo[CMDBuild.core.proxy.CMProxyConstants.TYPE]
				});
			} else {
				CMDBuild.Msg.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.errors.unmanagedReportType,
					false
				);
			}
		},

		onReportTypeSelected: function() {
			this.grid.getStore().load();
		},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.onReportTypeSelected();

				if (
					!Ext.isEmpty(node.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					&& node.get(CMDBuild.core.proxy.CMProxyConstants.ID) != CMDBuild.core.proxy.CMProxyConstants.CUSTOM
				) {
					this.createReport({
						id: node.get(CMDBuild.core.proxy.CMProxyConstants.ID),
						extension: node.get(CMDBuild.core.proxy.CMProxyConstants.TYPE).replace(/report/i, '') // Removes 'report' string from type property in node object
					});
				}
			}
		},

		/**
		 * Get created report from server and display it in popup window
		 */
		showReport: function() {
			var popup = window.open(
				CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory,
				'Report',
				'height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable'
			);

			if (!popup)
				CMDBuild.Msg.warn(
					CMDBuild.Translation.warnings.warning_message,
					CMDBuild.Translation.warnings.popup_block
				);
		}
	});

})();