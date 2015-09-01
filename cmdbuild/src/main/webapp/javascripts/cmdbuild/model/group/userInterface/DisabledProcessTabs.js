(function() {

	Ext.define('CMDBuild.model.group.userInterface.DisabledProcessTabs', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.PROCESS_ATTACHMENT_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.PROCESS_HISTORY_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.PROCESS_NOTE_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.PROCESS_RELATION_TAB, type: 'boolean' }
		]
	});

})();