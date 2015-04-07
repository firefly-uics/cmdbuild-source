(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.EmailPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],


		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		delegate: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		disabled: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.email
	});

})();