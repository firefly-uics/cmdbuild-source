(function() {

	Ext.define('CMDBuild.controller.management.reports.SingleReport', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.reports.Reports',
			'CMDBuild.model.reports.ModuleObject'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onSingleReportDownloadButtonClick',
			'onSingleReportTypeButtonClick',
			'showReport'
		],

		/**
		 * Parameters of last managed report
		 *
		 * @cfg {CMDBuild.model.reports.ModuleObject}
		 */
		managedReport: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedReportTypes: [
			CMDBuild.core.proxy.Constants.CSV,
			CMDBuild.core.proxy.Constants.ODT,
			CMDBuild.core.proxy.Constants.PDF,
			CMDBuild.core.proxy.Constants.RTF
		],

		/**
		 * @cfg {CMDBuild.view.management.reports.SingleReportPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} parameters
		 */
		createReport: function(parameters) {
			if (Ext.isObject(parameters) && !Ext.isEmpty(parameters[CMDBuild.core.proxy.Constants.ID])) {
				this.managedReportSet(parameters);

				CMDBuild.core.proxy.reports.Reports.create({
					params: this.managedReport.getData(),
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.error,
							CMDBuild.Translation.errors.createReportFilure,
							false
						);
					},
					success: function(response, options, decodedResponse) {
						if(decodedResponse.filled) { // Report with no parameters
							this.showReport();
						} else { // Show parameters window
							// FIX: in IE PDF is painted on top of the regular page content so remove it before display parameter window
							// Workaround to detect IE 11 witch is not supported from Ext 4.2
							if (Ext.isIE || !!navigator.userAgent.match(/Trident.*rv[ :]*11\./))
								this.view.removeAll();

							Ext.create('CMDBuild.controller.management.reports.Parameters', {
								parentDelegate: this,
								attributeList: decodedResponse.attribute,
								forceDownload: this.managedReport.get(CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD)
							});
						}
					}
				});
			}
		},

		onSingleReportDownloadButtonClick: function() {
			this.managedReport.set(CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD, true);

			this.createReport(this.managedReport.getData());
		},

		/**
		 * @param {String} type
		 */
		onSingleReportTypeButtonClick: function(type) {
			if (!Ext.Object.isEmpty(this.managedReport) && Ext.Array.contains(this.supportedReportTypes, type)) {
				var params = {};
				params[CMDBuild.core.proxy.Constants.ID] = this.managedReport.get(CMDBuild.core.proxy.Constants.ID);
				params[CMDBuild.core.proxy.Constants.EXTENSION] = type;

				this.createReport(params);
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
			if (
				!Ext.Object.isEmpty(node)
				&& !Ext.isEmpty(node.get(CMDBuild.core.proxy.Constants.ID))
				&& node.get(CMDBuild.core.proxy.Constants.ID) != CMDBuild.core.proxy.Constants.CUSTOM
			) {
				this.setViewTitle(node.get(CMDBuild.core.proxy.Constants.TEXT));

				this.managedReportSet(node.get(CMDBuild.core.proxy.Constants.ID));

				this.createReport({
					id: node.get(CMDBuild.core.proxy.Constants.ID),
					extension: node.get(CMDBuild.core.proxy.Constants.TYPE).replace(/report/i, '') // Removes 'report' string from type property in node object
				});

				this.callParent(arguments);
			}
		},

		// Managed report property methods
			/**
			 * @param {Object} parameters
			 */
			managedReportSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					this.managedReport = Ext.create('CMDBuild.model.reports.ModuleObject', parameters);
				} else {
					this.managedReport = null;
				}
			},

		/**
		 * Get created report from server and display it in popup window
		 */
		showReport: function() {
			if (!Ext.Object.isEmpty(this.managedReport))
				if (this.managedReport.get(CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD)) { // Force download mode
					var params = {};
					params[CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD_PARAM_KEY] = true;

					var form = Ext.create('Ext.form.Panel', {
						standardSubmit: true,
						url: CMDBuild.core.proxy.Index.reports.printReportFactory
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
							src: CMDBuild.core.proxy.Index.reports.printReportFactory
						}
					});
				}
		}
	});

})();