(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Server', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyWorkflow',
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'server',

		/**
		 * @property {CMDBuild.view.administration.configuration.ServerPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Main} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.configuration.ServerPanel', {
				delegate: this
			});

			this.cmOn('onReadConfiguration', {
				configFileName: this.configFileName,
				view: this.getView()
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
				case 'onServerClearCacheButtonClick':
					return this.onServerClearCacheButtonClick();

				case 'onServerServiceSynchButtonClick':
					return this.onServerServiceSynchButtonClick();

				case 'onServerUnlockCardsButtonClick':
					return this.onServerUnlockCardsButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {CMDBuild.view.administration.configuration.GeneralOptionsPanel}
		 */
		getView: function() {
			return this.view;
		},

		onServerClearCacheButtonClick: function() {
			CMDBuild.core.proxy.Utils.clearCache({
				success: CMDBuild.Msg.success
			});
		},

		onServerServiceSynchButtonClick: function() {
			CMDBuild.core.proxy.CMProxyWorkflow.synchronize({
				success: CMDBuild.Msg.success
			});
		},

		onServerUnlockCardsButtonClick: function() {
			CMDBuild.core.proxy.Card.unlockAllCards({
				success: CMDBuild.Msg.success
			});
		}
	});

})();