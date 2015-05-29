(function() {

	Ext.define('CMDBuild.controller.management.report.Modal', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportModalWindowDownloadButtonClick',
			'onReportModalWindowShow',
		],

		/**
		 * @cfg {CMDBuild.controller.management.report.Report}
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
		 * @property {CMDBuild.view.management.report.ModalWindow}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.ModalWindow', {
				delegate: this
			});

			this.setViewTitle(this.cmfg('managedReportGet').get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION));

			if (!Ext.isEmpty(this.view) && Ext.isString(this.format) && Ext.Array.contains(this.browserManagedFormats, this.format))
				this.view.show();
		},

		onReportModalWindowDownloadButtonClick: function() {
			var params = this.cmfg('managedReportGet');
			params.set(CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD, true);

			this.cmfg('createReport', params.getData());

			this.view.destroy();
		},

		onReportModalWindowShow: function() {
			if (!Ext.isEmpty(this.format))
				this.showReport();
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