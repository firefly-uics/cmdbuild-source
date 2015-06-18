(function() {

	Ext.define('CMDBuild.view.management.reports.ModalWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.reports.Modal}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.report,

		autoScroll: true,
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
						'->',
						Ext.create('CMDBuild.core.buttons.Download', {
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onReportModalWindowDownloadButtonClick');
							}
						})
					]
				}]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(window, eOpts) {
				this.delegate.cmfg('onReportModalWindowShow');
			}
		}
	});

})();