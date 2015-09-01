(function() {

	Ext.define('CMDBuild.model.group.userInterface.DisabledCardTabs', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTACHMENT_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_DETAIL_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_HISTORY_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NOTE_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_RELATION_TAB, type: 'boolean' }
		]
	});

})();