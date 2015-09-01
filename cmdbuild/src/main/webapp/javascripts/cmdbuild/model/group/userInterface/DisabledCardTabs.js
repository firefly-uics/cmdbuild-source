(function() {

	Ext.define('CMDBuild.model.group.userInterface.DisabledCardTabs', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.CLASS_ATTACHMENT_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CLASS_DETAIL_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CLASS_HISTORY_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CLASS_NOTE_TAB, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CLASS_RELATION_TAB, type: 'boolean' }
		]
	});

})();