(function() {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.report.Print'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onPrintWindowDownloadButtonClick',
			'onPrintWindowShow',
		],

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
		 * @cfg {Boolean}
		 */
		forceDownload: false,

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
		 * @property {CMDBuild.view.common.entryTypeGrid.printTool.PrintWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.printTool.PrintWindow', { delegate: this });

			if (!Ext.isEmpty(this.view) && Ext.isString(this.format))
				if (Ext.Array.contains(this.browserManagedFormats, this.format)) { // With browser managed formats show modal pop-up
					this.view.show();
				} else { // Otherwise force file download
					this.forceDownload = true;

					this.createDocument();
				}
		},

		createDocument: function() {
			var proxyCreateFunction = null;

			switch (this.mode) {
				case 'cardDetails': {
					proxyCreateFunction = CMDBuild.core.proxy.report.Print.createCardDetails;
				} break;

				case 'classSchema': {
					proxyCreateFunction = CMDBuild.core.proxy.report.Print.createClassSchema;
				} break;

				case 'dataViewSql': {
					proxyCreateFunction = CMDBuild.core.proxy.report.Print.createDataViewSqlSchema;
				} break;

				case 'schema': {
					proxyCreateFunction = CMDBuild.core.proxy.report.Print.createSchema;
				} break;

				case 'view': {
					proxyCreateFunction = CMDBuild.core.proxy.report.Print.createView;
				} break;

				default: {
					_error('unmanaged print window mode', this);
				}
			}

			if (!Ext.isEmpty(proxyCreateFunction))
				proxyCreateFunction({
					params: this.parameters,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.showReport();
					}
				});
		},

		onPrintWindowDownloadButtonClick: function() {
			this.forceDownload = true;

			this.createDocument();
		},

		onPrintWindowShow: function() {
			this.forceDownload = false; // Reset value on show

			if (!Ext.isEmpty(this.format))
				this.createDocument();
		},

		/**
		 * Get created report from server and display it in iframe
		 */
		showReport: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD_PARAM_KEY] = true;

			if (this.forceDownload) { // Force download mode
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