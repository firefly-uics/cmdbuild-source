(function() {

	Ext.define('CMDBuild.controller.management.report.Modal', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
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
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportModalWindowDownloadButtonClick',
			'onReportModalWindowShow',
		],

		/**
		 * @cfg {String}
		 */
		extension: CMDBuild.core.proxy.CMProxyConstants.PDF,

		/**
		 * @property {CMDBuild.view.management.report.ModalWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.ModalWindow', { delegate: this });

			this.setViewTitle(this.cmfg('currentReportRecordGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION));

			if (!Ext.isEmpty(this.view) && Ext.isString(this.extension) && Ext.Array.contains(this.browserManagedFormats, this.extension))
				this.view.show();
		},

		onReportModalWindowDownloadButtonClick: function() {
			this.cmfg('showReport', true);
		},

		onReportModalWindowShow: function() {
			if (!Ext.isEmpty(this.extension)) {
				this.view.removeAll();

				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory + '?donotdelete=true' // Add parameter to avoid report delete
					}
				});
			}
		}
	});

})();