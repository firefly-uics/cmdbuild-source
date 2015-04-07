(function () {

	/**
	 * Extends original view to implement function to enable tab
	 */
	Ext.define('CMDBuild.view.management.workflow.tabs.Email', {
		extend: 'CMDBuild.view.management.common.tabs.email.EmailPanel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],


		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		delegate: undefined,

		cmActivate: function() {
			this.setDisabled(false);

			this.delegate.parentDelegate.view.cardTabPanel.acutalPanel.setActiveTab(this);
		}
	});

})();