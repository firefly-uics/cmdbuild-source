(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Server', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyWorkflow',
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onServerClearCacheButtonClick',
			'onServerServiceSynchButtonClick',
			'onServerUnlockCardsButtonClick'
		],

		/**
		 * Proxy parameters
		 *
		 * @cfg {Object}
		 */
		params: {
			fileName: 'server',
			view: undefined
		},

		/**
		 * @property {CMDBuild.view.administration.configuration.ServerPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.ServerPanel', {
				delegate: this
			});

			this.params[CMDBuild.core.proxy.Constants.VIEW] = this.view;

			this.cmfg('onConfigurationRead', this.params);
		},

		onServerClearCacheButtonClick: function() {
			CMDBuild.core.proxy.Utils.clearCache({
				success: CMDBuild.core.Message.success
			});
		},

		onServerServiceSynchButtonClick: function() {
			CMDBuild.core.proxy.CMProxyWorkflow.synchronize({
				success: CMDBuild.core.Message.success
			});
		},

		onServerUnlockCardsButtonClick: function() {
			CMDBuild.core.proxy.Card.unlockAllCards({
				success: CMDBuild.core.Message.success
			});
		}
	});

})();