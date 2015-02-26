(function() {

	Ext.define('CMDBuild.controller.management.report.SingleReport', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report'
		],

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
		 * @param {Object} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

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
				case 'onReportTypeButtonClick' :
					return this.onReportTypeButtonClick(param);

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
							if (Ext.isIE) // FIX: in IE PDF is painted on top of the regular page content so remove it before display parameter window
								this.view.removeAll();

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
				&& node.get(CMDBuild.core.proxy.CMProxyConstants.ID) != 'custom'
			) {
				this.view.setTitle(this.view.sectionTitle + ' - ' + node.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				this.reportId = node.get(CMDBuild.core.proxy.CMProxyConstants.ID);

				this.createReport({
					id: node.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					extension: node.get(CMDBuild.core.proxy.CMProxyConstants.TYPE).replace(/report/i, '') // Removes 'report' string from type property in node object
				});
			}
		},

		/**
		 * Get created report from server and display it in iframe
		 */
		showReport: function() {
			this.view.removeAll();

			this.view.add({
				xtype: 'component',

				autoEl: {
					tag: 'iframe',
					src: CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory
				}
			});
		}
	});

})();