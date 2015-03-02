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
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'top',
					itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
					items: [
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.pdf,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.PDF,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.PDF);
							}
						}),
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.odt,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.ODT,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.ODT);
							}
						}),
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.rtf,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.RTF,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.RTF);
							}
						}),
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.csv,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.CSV,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.CSV);
							}
						}),
						'->',
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.download,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportDownloadButtonClick');
							}
						})
					]
				}]
			});

			this.callParent(arguments);
		}
	});

})();