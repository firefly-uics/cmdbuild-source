(function() {

	Ext.define('CMDBuild.controller.management.reports.Modal', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportModalWindowDownloadButtonClick',
			'onReportModalWindowShow',
		],

		/**
		 * @cfg {CMDBuild.controller.management.reports.Reports}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		browserManagedFormats: [
			CMDBuild.core.proxy.Constants.PDF,
			CMDBuild.core.proxy.Constants.CSV
		],

		/**
		 * @cfg {String}
		 */
		format: CMDBuild.core.proxy.Constants.PDF,

		/**
		 * @property {CMDBuild.view.management.reports.ModalWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.reports.ModalWindow', {
				delegate: this
			});

			this.setViewTitle(this.cmfg('managedReportGet').get(CMDBuild.core.proxy.Constants.DESCRIPTION));

			if (!Ext.isEmpty(this.view) && Ext.isString(this.format) && Ext.Array.contains(this.browserManagedFormats, this.format))
				this.view.show();
		},

		onReportModalWindowDownloadButtonClick: function() {
			var params = this.cmfg('managedReportGet');
			params.set(CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD, true);

			this.cmfg('createReport', params.getData());

			this.view.destroy();
		},

		onReportModalWindowShow: function() {
			if (!Ext.isEmpty(this.format))
				this.showReport();
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
					src: CMDBuild.core.proxy.Index.reports.printReportFactory
				}
			});
		}
	});

})();