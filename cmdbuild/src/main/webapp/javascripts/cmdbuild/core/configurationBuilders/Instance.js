(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Instance', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		constructor: function() {
			if (
				!Ext.isEmpty(CMDBuild)
				&& !Ext.isEmpty(CMDBuild.configuration)
			) {
				CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE] = Ext.create('CMDBuild.model.configuration.Instance'); // Instance configuration object

				var configurationObject = CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE]; // Shorthand

				CMDBuild.core.proxy.Configuration.readMainConfiguration({
					scope: this,
					success: function(result, options, decodedResult) {
						// TODO: waiting for refactor (server attributes translation)
						configurationObject.set(CMDBuild.core.constants.Proxy.CARD_FORM_RATIO, decodedResult.data['grid_card_ratio']);
						configurationObject.set(CMDBuild.core.constants.Proxy.CARD_LOCK_TIMEOUT, decodedResult.data['lockcardtimeout']);
						configurationObject.set(CMDBuild.core.constants.Proxy.CARD_TABS_POSITION, decodedResult.data['card_tab_position']);
						configurationObject.set(CMDBuild.core.constants.Proxy.DISPLAY_CARD_LOCKER_NAME, decodedResult.data['lockcarduservisible']);
						configurationObject.set(CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK, decodedResult.data['lockcardenabled']);
						configurationObject.set(CMDBuild.core.constants.Proxy.INSTANCE_NAME, decodedResult.data['instance_name']);
						configurationObject.set(CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE, decodedResult.data['popuppercentageheight']);
						configurationObject.set(CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE, decodedResult.data['popuppercentagewidth']);
						configurationObject.set(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT, decodedResult.data['referencecombolimit']);
						configurationObject.set(CMDBuild.core.constants.Proxy.RELATION_LIMIT, decodedResult.data['relationlimit']);
						configurationObject.set(CMDBuild.core.constants.Proxy.ROW_LIMIT, decodedResult.data['rowlimit']);
						configurationObject.set(CMDBuild.core.constants.Proxy.SESSION_TIMEOUT, decodedResult.data['session.timeout']);
						configurationObject.set(CMDBuild.core.constants.Proxy.STARTING_CLASS, decodedResult.data['startingclass']);
					}
				});
			} else {
				_error('CMDBuild or CMDBuild.configuration objects are empty', this);
			}
		}
	});

})();