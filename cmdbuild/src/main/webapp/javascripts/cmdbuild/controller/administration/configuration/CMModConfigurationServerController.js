(function() {

	Ext.define('CMDBuild.controller.administration.configuration.CMModConfigurationServerController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		/**
		 * @property {CMDBuild.view.administration.configuration.CMModConfigurationServer}
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

		onConfigurationServerClearCacheButtonClick: function() { // TODO proxy
			CMDBuild.Ajax.request( {
				url: 'services/json/utils/clearcache',
				loadMask: true,
				success: CMDBuild.Msg.success
			});
		},

		onConfigurationServerServiceSynchButtonClick: function() { // TODO proxy
			CMDBuild.Ajax.request( {
				url : 'services/json/workflow/sync',
				loadMask : true,
				success : CMDBuild.Msg.success
			});
		},

		onConfigurationServerUnlockCardsButtonClick: function() { // TODO proxy
			_CMProxy.card.unlockAllCards({
				success : CMDBuild.Msg.success
			});
		},
	});

})();