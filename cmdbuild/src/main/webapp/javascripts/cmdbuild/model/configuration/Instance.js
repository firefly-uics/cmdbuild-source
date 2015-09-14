(function() {

	Ext.define('CMDBuild.model.configuration.Instance', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.CARD_GRID_RATIO, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.CARD_LOCK_TIMEOUT, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.CARD_TABS_POSITION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DISPLAY_CARD_LOCKER_NAME, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.ENABLE_CARD_LOCK, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.INSTANCE_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.POPUP_HEIGHT_PERCENTAGE, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.POPUP_WIDTH_PERCENTAGE, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.REFERENCE_COMBO_STORE_LIMIT, type: 'int' }, // Limit of dropDown displayed relations before force pop-up window show
			{ name: CMDBuild.core.proxy.Constants.RELATION_LIMIT, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.ROW_LIMIT, type: 'int' }, // Rows per page to display in card's grid
			{ name: CMDBuild.core.proxy.Constants.SESSION_TIMEOUT, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.STARTING_CLASS, type: 'string' } // Default selected class on UI display
		]
	});

})();