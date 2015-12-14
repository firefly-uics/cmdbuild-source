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
		baseTitle: CMDBuild.Translation.report,

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
							Ext.create('CMDBuild.core.buttons.fileTypes.Pdf', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.PDF);
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Odt', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.ODT);
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Rtf', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.RTF);
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Csv', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.CSV);
								}
							}),
							'->',
							Ext.create('CMDBuild.core.buttons.iconized.Download', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportDownloadButtonClick');
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