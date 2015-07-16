(function() {

	Ext.define('CMDBuild.view.management.reports.SingleReportPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.management.reports.SingleReport}
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
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'top',
					itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,
					items: [
						Ext.create('CMDBuild.core.buttons.fileTypes.Pdf', {
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.Constants.PDF);
							}
						}),
						Ext.create('CMDBuild.core.buttons.fileTypes.Odt', {
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.Constants.ODT);
							}
						}),
						Ext.create('CMDBuild.core.buttons.fileTypes.Rtf', {
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.Constants.RTF);
							}
						}),
						Ext.create('CMDBuild.core.buttons.fileTypes.Csv', {
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.proxy.Constants.CSV);
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
				}]
			});

			this.callParent(arguments);
		}
	});

})();