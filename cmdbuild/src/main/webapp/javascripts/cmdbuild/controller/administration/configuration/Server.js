(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Server', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyWorkflow',
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.Utils',
			'CMDBuild.core.proxy.processes.Activity'
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
		 * @cfg {String}
		 */
		configFileName: 'server',

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

			this.cmfg('onConfigurationRead', {
				configFileName: this.configFileName,
				view: this.view
			});
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

		/**
		 * Unlocks all cards and processes also if proxy is specific for cards
		 */
		onServerUnlockCardsButtonClick: function() {
			CMDBuild.core.proxy.Card.unlockAll({
				success: CMDBuild.Msg.success
			});
		}
	});

})();