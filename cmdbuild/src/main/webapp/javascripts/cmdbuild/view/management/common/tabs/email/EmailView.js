(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.EmailView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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
		},

		/**
		 * Service function executed from module controller
		 */
		reset: function() {
			this.setDisabled(Ext.isEmpty(this.delegate.cmfg('selectedEntityGet').get(CMDBuild.core.proxy.CMProxyConstants.ENTITY)));
		}
	});

})();