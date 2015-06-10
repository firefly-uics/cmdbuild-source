(function() {

	Ext.define('CMDBuild.controller.management.reports.SingleReport', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
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
			CMDBuild.core.proxy.CMProxyConstants.CSV,
			CMDBuild.core.proxy.CMProxyConstants.ODT,
			CMDBuild.core.proxy.CMProxyConstants.PDF,
			CMDBuild.core.proxy.CMProxyConstants.RTF
		],

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @cfg {CMDBuild.view.management.reports.SingleReportPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} parameters
		 */
		createReport: function(parameters) {
			if (Ext.isObject(parameters) && !Ext.isEmpty(parameters[CMDBuild.core.proxy.CMProxyConstants.ID])) {
				this.managedReportSet(parameters);

				CMDBuild.core.proxy.reports.Reports.create({
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
							if (Ext.isIE) // FIX: in IE PDF is painted on top of the regular page content so remove it before display parameter window
								this.view.removeAll();

							Ext.create('CMDBuild.controller.management.reports.Parameters', {
								parentDelegate: this,
								attributeList: decodedResponse.attribute,
								forceDownload: this.managedReport.get(CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD)
							});
						}
					}
				});
			}
		},

		onSingleReportDownloadButtonClick: function() {
			this.managedReport.set(CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD, true);

			this.createReport(this.managedReport.getData());
		},

		/**
		 * @param {String} type
		 */
		onSingleReportTypeButtonClick: function(type) {
			if (!Ext.Object.isEmpty(this.managedReport) && Ext.Array.contains(this.supportedReportTypes, type)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ID] = this.managedReport.get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.EXTENSION] = type;

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
			if (
				!Ext.Object.isEmpty(node)
				&& !Ext.isEmpty(node.get(CMDBuild.core.proxy.CMProxyConstants.ID))
				&& node.get(CMDBuild.core.proxy.CMProxyConstants.ID) != CMDBuild.core.proxy.CMProxyConstants.CUSTOM
			) {
				this.setViewTitle(node.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				this.managedReportSet(node.get(CMDBuild.core.proxy.CMProxyConstants.ID));

				this.createReport({
					id: node.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					extension: node.get(CMDBuild.core.proxy.CMProxyConstants.TYPE).replace(/report/i, '') // Removes 'report' string from type property in node object
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
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (Ext.isEmpty(titlePart)) {
				this.view.setTitle(this.view.baseTitle);
			} else {
				this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
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