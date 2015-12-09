(function() {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Index',
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
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.CSV
		],

		/**
		 * @cfg {String}
		 */
		format: CMDBuild.core.constants.Proxy.PDF,

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
					this.createDocument(true);
				}
		},

		/**
		 * @private
		 */
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
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.error,
							CMDBuild.Translation.errors.createReportFilure,
							false
						);
					},
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
		 *
		 * @private
		 */
		showReport: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			if (this.forceDownload) { // Force download mode
				var form = Ext.create('Ext.form.Panel', {
					standardSubmit: true,
					url: CMDBuild.core.proxy.Index.report.printReportFactory
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
						src: CMDBuild.core.proxy.Index.report.printReportFactory
					}
				});
			}
		}
	});

})();