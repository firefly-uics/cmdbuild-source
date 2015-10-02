(function() {

	Ext.define('CMDBuild.view.common.entryTypeGrid.printTool.PrintWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow}
		 */
		delegate: undefined,

		border: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.printPreview,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'top',
					itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
					items: [
						'->',
						Ext.create('CMDBuild.core.buttons.iconized.Download', {
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onPrintWindowDownloadButtonClick');
							}
						})
					]
				}]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(window, eOpts) {
				this.delegate.cmfg('onPrintWindowShow');
			}
		}
	});

})();