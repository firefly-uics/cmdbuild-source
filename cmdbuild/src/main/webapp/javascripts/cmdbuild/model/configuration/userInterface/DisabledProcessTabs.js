(function() {

	Ext.define('CMDBuild.model.configuration.userInterface.DisabledProcessTabs', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_ATTACHMENT_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_EMAIL_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_HISTORY_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_NOTE_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_RELATION_TAB, type: 'boolean' }
		]
	});

})();