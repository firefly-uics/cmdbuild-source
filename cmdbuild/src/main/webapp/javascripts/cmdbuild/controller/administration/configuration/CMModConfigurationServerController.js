(function() {

	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationServerController", {
		extend: "CMDBuild.controller.common.CMBasePanelController",

		/**
		 * @property {CMDBuild.view.administration.configuration.CMModConfigurationServer}
		 */
		view: undefined,

		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.view.delegate = this;

			this.view.clearCacheButton.on("click", function() {
				CMDBuild.Ajax.request( {
					url : 'services/json/utils/clearcache',
					loadMask : true,
					success : CMDBuild.Msg.success
				});
			});

			this.view.clearProcesses.on("click", function() {
				CMDBuild.Ajax.request( {
					url : 'services/json/workflow/sync',
					loadMask : true,
					success : CMDBuild.Msg.success
				});
			});

			this.view.unlockAllCards.on("click", function() {
				_CMProxy.card.unlockAllCards({
					success : CMDBuild.Msg.success
				});
			});
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
				case 'onConfigurationAbortButtonClick':
					return this.onConfigurationAbortButtonClick();

				case 'onConfigurationSaveButtonClick':
					return this.onConfigurationSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},
	});

})();