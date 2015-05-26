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
		columns: [],

		/**
		 * @cfg {Object}
		 */
		extraParams: {},

		/**
		 * @cfg {String}
		 */
		format: CMDBuild.core.proxy.CMProxyConstants.PDF,

		/**
		 * @cfg {Array}
		 */
		sort: [],

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
				this.view.show();
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		createDocument: function(forceDownload) {
			var params = Ext.apply({}, this.extraParams);
			params[CMDBuild.core.proxy.CMProxyConstants.COLUMNS] = Ext.encode(this.columns);
			params[CMDBuild.core.proxy.CMProxyConstants.SORT] = Ext.encode(this.sort);
			params[CMDBuild.core.proxy.CMProxyConstants.TYPE] = this.format;

			CMDBuild.core.proxy.Report.createViewReport({
				params: params,
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