(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Server', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyWorkflow',
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.Utils'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.ServerPanel}
		 */
		view: undefined,

		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.view.delegate = this;
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onConfigurationServerClearCacheButtonClick':
					return this.onConfigurationServerClearCacheButtonClick();

				case 'onConfigurationServerServiceSynchButtonClick':
					return this.onConfigurationServerServiceSynchButtonClick();

				case 'onConfigurationServerUnlockCardsButtonClick':
					return this.onConfigurationServerUnlockCardsButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onConfigurationServerClearCacheButtonClick: function() {
			CMDBuild.core.proxy.Utils.clearCache({
				success: CMDBuild.Msg.success
			});
		},

		onConfigurationServerServiceSynchButtonClick: function() {
			CMDBuild.core.proxy.CMProxyWorkflow.synchronize({
				success: CMDBuild.Msg.success
			});
		},

		onConfigurationServerUnlockCardsButtonClick: function() {
			CMDBuild.core.proxy.Card.unlockAllCards({
				success: CMDBuild.Msg.success
			});
		}
	});

})();