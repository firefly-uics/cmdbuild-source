(function() {

	Ext.define('CMDBuild.controller.management.common.entryTypeGrid.printTool.PrintWindow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Report'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onPrintWindowDownloadButtonClick',
			'onPrintWindowShow',
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		browserManagedFormats: [
			CMDBuild.core.proxy.CMProxyConstants.PDF,
			CMDBuild.core.proxy.CMProxyConstants.CSV
		],

		/**
		 * @cfg {String}
		 */
		format: CMDBuild.core.proxy.CMProxyConstants.PDF,

		/**
		 * Utilization mode
		 *
		 * @cfg {String}
		 */
		mode: undefined,

		/**
		 * @cfg {Object}
		 */
		parameters: undefined,

		/**
		 * @property {CMDBuild.view.management.common.entryTypeGrid.printTool.PrintWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.entryTypeGrid.printTool.PrintWindow', {
				delegate: this
			});

			if (!Ext.isEmpty(this.view) && Ext.isString(this.format))
				if (Ext.Array.contains(this.browserManagedFormats, this.format)) { // With browser managed formats show modal pop-up
					this.view.show();
				} else { // Otherwise force file download
					this.createDocument(true);
				}
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		createDocument: function(forceDownload) { // TODO ottimizzare cambiando solo il nome del metodo???
			switch (this.mode) {
				case 'cardDetails': {
					CMDBuild.core.proxy.Report.createCardDetailsReport({
						params: this.parameters,
						scope: this,
						failure: function(response, options, decodedResponse) {
							CMDBuild.Msg.error(
								CMDBuild.Translation.error,
								CMDBuild.Translation.errors.createReportFilure,
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.showReport(forceDownload);
						}
					});
				} break;

				case 'classSchema': {
					CMDBuild.core.proxy.Report.createClassSchemaReport({
						params: this.parameters,
						scope: this,
						failure: function(response, options, decodedResponse) {
							CMDBuild.Msg.error(
								CMDBuild.Translation.error,
								CMDBuild.Translation.errors.createReportFilure,
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.showReport(forceDownload);
						}
					});
				} break;

				case 'schema': {
					CMDBuild.core.proxy.Report.createSchemaReport({
						params: this.parameters,
						scope: this,
						failure: function(response, options, decodedResponse) {
							CMDBuild.Msg.error(
								CMDBuild.Translation.error,
								CMDBuild.Translation.errors.createReportFilure,
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.showReport(forceDownload);
						}
					});
				} break;

				case 'view': {
					CMDBuild.core.proxy.Report.createViewReport({
						params: this.parameters,
						scope: this,
						failure: function(response, options, decodedResponse) {
							CMDBuild.Msg.error(
								CMDBuild.Translation.error,
								CMDBuild.Translation.errors.createReportFilure,
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.showReport(forceDownload);
						}
					});
				} break;

				default: {
					_error('unmanaged print window mode', this);
				}
			}
		},

		onPrintWindowDownloadButtonClick: function() {
			this.createDocument(true);
		},

		onPrintWindowShow: function() {
			if (!Ext.isEmpty(this.format))
				this.createDocument();
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