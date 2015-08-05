(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Instance', {

		requires: ['CMDBuild.core.proxy.Constants'],

		constructor: function() {
			if (
				!Ext.isEmpty(CMDBuild)
				&& !Ext.isEmpty(CMDBuild.configuration)
			) {
				CMDBuild.configuration[CMDBuild.core.proxy.Constants.INSTANCE] = Ext.create('CMDBuild.model.configuration.Instance'); // Instance configuration object

				var configurationObject = CMDBuild.configuration[CMDBuild.core.proxy.Constants.INSTANCE]; // Shorthand

				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					scope: this,
					success: function(result, options, decodedResult) {
						// Attributes translation waiting for a server side refactor
						configurationObject.set(CMDBuild.core.proxy.Constants.CARD_GRID_RATIO, decodedResult.data['grid_card_ratio']);
						configurationObject.set(CMDBuild.core.proxy.Constants.CARD_LOCK_TIMEOUT, decodedResult.data['lockcardtimeout']);
						configurationObject.set(CMDBuild.core.proxy.Constants.CARD_TABS_POSITION, decodedResult.data['card_tab_position']);
						configurationObject.set(CMDBuild.core.proxy.Constants.DISPLAY_CARD_LOCKER_NAME, decodedResult.data['lockcarduservisible']);
						configurationObject.set(CMDBuild.core.proxy.Constants.ENABLE_CARD_LOCK, decodedResult.data['lockcardenabled']);
						configurationObject.set(CMDBuild.core.proxy.Constants.INSTANCE_NAME, decodedResult.data['instance_name']);
						configurationObject.set(CMDBuild.core.proxy.Constants.POPUP_HEIGHT_PERCENTAGE, decodedResult.data['popuppercentageheight']);
						configurationObject.set(CMDBuild.core.proxy.Constants.POPUP_WIDTH_PERCENTAGE, decodedResult.data['popuppercentagewidth']);
						configurationObject.set(CMDBuild.core.proxy.Constants.REFERENCE_COMBO_STORE_LIMIT, decodedResult.data['referencecombolimit']);
						configurationObject.set(CMDBuild.core.proxy.Constants.RELATION_LIMIT, decodedResult.data['relationlimit']);
						configurationObject.set(CMDBuild.core.proxy.Constants.ROW_LIMIT, decodedResult.data['rowlimit']);
						configurationObject.set(CMDBuild.core.proxy.Constants.SESSION_TIMEOUT, decodedResult.data['session.timeout']);
						configurationObject.set(CMDBuild.core.proxy.Constants.STARTING_CLASS, decodedResult.data['startingclass']);
					}
				});
			} else {
				_error('CMDBuild or CMDBuild.configuration objects are empty', this);
			}
		}
	});

})();