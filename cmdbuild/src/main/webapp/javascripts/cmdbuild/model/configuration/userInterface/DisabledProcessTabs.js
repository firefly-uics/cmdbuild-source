(function() {

	Ext.define('CMDBuild.model.configuration.userInterface.DisabledProcessTabs', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.PROCESS_ATTACHMENT_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS_EMAIL_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS_HISTORY_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS_NOTE_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS_RELATION_TAB, type: 'boolean' }
		]
	});

})();