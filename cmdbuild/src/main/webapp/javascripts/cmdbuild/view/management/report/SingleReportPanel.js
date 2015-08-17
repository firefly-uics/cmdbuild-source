(function() {

	Ext.define('CMDBuild.view.management.report.SingleReportPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.report.SingleReport}
		 */
		delegate: undefined,

		/**
		 * @param {Number}
		 */
		reportId: undefined,

		/**
		 * @cfg {String}
		 */
		sectionTitle: CMDBuild.Translation.report,

		border: true,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.FileFormatsPdf', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.PDF);
								}
							}),
							Ext.create('CMDBuild.core.buttons.FileFormatsOdt', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.ODT);
								}
							}),
							Ext.create('CMDBuild.core.buttons.FileFormatsRtf', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.RTF);
								}
							}),
							Ext.create('CMDBuild.core.buttons.FileFormatsCsv', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.CSV);
								}
							}),
							'->',
							Ext.create('CMDBuild.core.buttons.Download', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportDownloadButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();