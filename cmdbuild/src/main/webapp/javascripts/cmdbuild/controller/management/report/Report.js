(function() {

	/**
	 * This class uses only partially currentReportParameters methods, because update functionalities to keep parameters selection aren't required.
	 */
	Ext.define('CMDBuild.controller.management.report.Report', {
		extend: 'CMDBuild.controller.management.report.SingleReport',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.report.Report'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'currentReportParametersGet',
			'currentReportParametersSet',
			'currentReportRecordGet',
			'onReportGenerateButtonClick',
			'showReport',
			'updateReport'
		],

		/**
		 * @property {CMDBuild.model.report.Grid}
		 *
		 * @private
		 */
		currentReportRecord: undefined,

		/**
		 * Witch report types will be viewed inside modal pop-up
		 *
		 * @cfg {Array}
		 */
		forceDownloadTypes: [
			CMDBuild.core.constants.Proxy.ODT,
			CMDBuild.core.constants.Proxy.RTF
		],

		/**
		 * @property {CMDBuild.view.management.report.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Array}
		 */
		managedReportTypes: [
			CMDBuild.core.constants.Proxy.CSV,
			CMDBuild.core.constants.Proxy.ODT,
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.RTF
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

			this.grid = Ext.create('CMDBuild.view.management.report.GridPanel', { delegate: this });

			this.view.add(this.grid);
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		createReport: function(forceDownload) {
			forceDownload = forceDownload || false;

			if (
				!Ext.isEmpty(this.currentReportParametersGet({
					callIdentifier: 'create',
					property: CMDBuild.core.constants.Proxy.ID
				}))
			) {
				CMDBuild.core.proxy.report.Report.create({
					params: this.currentReportParametersGet({ callIdentifier: 'create' }),
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

		// CurrentReportRecord methods
			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.report.Grid} or Mixed
			 */
			currentReportRecordGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.currentReportRecord.get(parameterName);

				return this.currentReportRecord;
			},

			/**
			 * @param {CMDBuild.model.report.Grid} record
			 */
			currentReportRecordSet: function(record) {
				this.currentReportRecord = null;

				if (!Ext.isEmpty(record))
					this.currentReportRecord = record;
			},

		/**
		 * @param {Object} reportInfo
		 */
		onReportGenerateButtonClick: function(reportInfo) {
			if (Ext.Array.contains(this.managedReportTypes, reportInfo[CMDBuild.core.constants.Proxy.TYPE])) {
				this.currentReportParametersSet({
					callIdentifier: 'create',
					params: {
						extension: reportInfo[CMDBuild.core.constants.Proxy.TYPE],
						id: reportInfo[CMDBuild.core.constants.Proxy.RECORD].get(CMDBuild.core.constants.Proxy.ID)
					}
				});

				this.currentReportRecordSet(reportInfo[CMDBuild.core.constants.Proxy.RECORD]);

				// Force download true for PDF and CSV
				this.createReport(Ext.Array.contains(this.forceDownloadTypes, reportInfo[CMDBuild.core.constants.Proxy.TYPE]));
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
					!Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.ID))
					&& node.get(CMDBuild.core.constants.Proxy.ID) != CMDBuild.core.constants.Proxy.CUSTOM
				) {
					this.currentReportParametersSet({
						callIdentifier: 'create',
						params: {
							extension: node.get(CMDBuild.core.constants.Proxy.TYPE).replace(/report/i, ''), // Removes 'report' string from type property in node object
							id: node.get(CMDBuild.core.constants.Proxy.ID),
						}
					});

					this.createReport();

					this.callParent(arguments);
				}
			}
		},

		/**
		 * Get created report from server and display it in popup window
		 *
		 * @param {Boolean} forceDownload
		 */
		showReport: function(forceDownload) {
			forceDownload = forceDownload || false;

			if (forceDownload) { // Force download mode
				var params = {};
				params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

				var form = Ext.create('Ext.form.Panel', {
					standardSubmit: true,
					url: CMDBuild.core.proxy.Index.reports.printReportFactory + '?donotdelete=true' // Add parameter to avoid report delete
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
					extension: this.currentReportParametersGet({
						callIdentifier: 'create',
						property: CMDBuild.core.constants.Proxy.EXTENSION
					})
				});
			}
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		updateReport: function(forceDownload) {
			if (!this.currentReportParametersIsEmpty('update')) {
				CMDBuild.core.proxy.report.Report.update({
					params: this.currentReportParametersGet({ callIdentifier: 'update' }),
					scope: this,
					success: function(response, options, decodedResponse) {
						this.showReport(forceDownload);
					}
				});
			}
		}
	});

})();