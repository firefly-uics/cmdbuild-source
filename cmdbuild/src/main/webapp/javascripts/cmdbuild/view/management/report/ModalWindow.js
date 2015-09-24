(function() {

	Ext.define('CMDBuild.view.management.report.ModalWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.report.Modal}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.report,

		border: true,
		frame: false,
		layout: 'fit',
		overflowY: true,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							'->',
							Ext.create('CMDBuild.core.buttons.iconized.Download', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportModalWindowDownloadButtonClick');
								}
							})
						]
					})
				]
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