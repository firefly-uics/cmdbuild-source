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

		frame: true,
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
							textAlign: 'left',
							minWidth: 80,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.PDF);
							}
						}),
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.odt,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.ODT,
							textAlign: 'left',
							minWidth: 80,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.ODT);
							}
						}),
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.rtf,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.RTF,
							textAlign: 'left',
							minWidth: 80,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.RTF);
							}
						}),
						Ext.create('Ext.button.Button', {
							text: CMDBuild.Translation.csv,
							iconCls: CMDBuild.core.proxy.CMProxyConstants.CSV,
							textAlign: 'left',
							minWidth: 80,
							scope: this,

							handler: function() {
								this.delegate.cmOn('onReportTypeButtonClick', CMDBuild.core.proxy.CMProxyConstants.CSV);
							}
						})
					]
				}]
			});

			this.callParent(arguments);
		}
	});

})();