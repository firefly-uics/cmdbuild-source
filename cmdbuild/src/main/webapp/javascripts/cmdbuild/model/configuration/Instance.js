(function() {

	Ext.define('CMDBuild.model.configuration.Instance', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CARD_GRID_RATIO, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.CARD_LOCK_TIMEOUT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.CARD_TABS_POSITION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISPLAY_CARD_LOCKER_NAME, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.INSTANCE_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT, type: 'int' }, // Limit of dropDown displayed relations before force pop-up window show
			{ name: CMDBuild.core.constants.Proxy.RELATION_LIMIT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.ROW_LIMIT, type: 'int' }, // Rows per page to display in card's grid
			{ name: CMDBuild.core.constants.Proxy.SESSION_TIMEOUT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.STARTING_CLASS, type: 'string' } // Default selected class on UI display
		]
	});

})();