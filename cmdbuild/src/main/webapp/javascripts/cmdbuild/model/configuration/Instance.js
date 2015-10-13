(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.configuration.Instance', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CARD_FORM_RATIO, type: 'int', defaultValue: 50 },
			{ name: CMDBuild.core.constants.Proxy.CARD_LOCK_TIMEOUT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.CARD_TABS_POSITION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISPLAY_CARD_LOCKER_NAME, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.INSTANCE_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT, type: 'int' }, // Limit of dropDown displayed relations before force pop-up window show
			{ name: CMDBuild.core.constants.Proxy.RELATION_LIMIT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.ROW_LIMIT, type: 'int', defaultValue: 20 }, // Rows per page to display in card's grid
			{ name: CMDBuild.core.constants.Proxy.SESSION_TIMEOUT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.STARTING_CLASS, type: 'string' } // Default selected class on UI display
		]
	});

})();