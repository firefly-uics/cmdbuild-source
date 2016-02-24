(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Server', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.CMProxyWorkflow',
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
			'onConfigurationServerClearCacheButtonClick',
			'onConfigurationServerServiceSynchButtonClick',
			'onConfigurationServerUnlockCardsButtonClick'
		],

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

			this.view = Ext.create('CMDBuild.view.administration.configuration.ServerPanel', { delegate: this });
		},

		onConfigurationServerClearCacheButtonClick: function() {
			CMDBuild.core.proxy.Utils.clearCache({
				success: CMDBuild.core.Message.success
			});
		},

		onConfigurationServerServiceSynchButtonClick: function() {
			CMDBuild.core.proxy.CMProxyWorkflow.synchronize({
				success: CMDBuild.core.Message.success
			});
		},

		/**
		 * Unlocks all cards and processes also if proxy is specific for cards
		 */
		onConfigurationServerUnlockCardsButtonClick: function() {
			CMDBuild.core.proxy.Card.unlockAll({
				success: CMDBuild.core.Message.success
			});
		}
	});

})();