(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.EmailView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		delegate: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.email,

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onEmailPanelShow');
			}
		}
	});

})();