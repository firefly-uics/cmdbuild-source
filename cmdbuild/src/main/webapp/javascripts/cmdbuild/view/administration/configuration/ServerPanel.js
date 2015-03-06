(function() {

	Ext.define('CMDBuild.view.administration.configuration.ServerPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Server}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.setup,

		/**
		 * @cfg {String}
		 */
		configFileName: 'server',

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.serverManagement,
				items: [
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.cacheManagement,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: CMDBuild.Translation.clearCache,
								scope: this,

								handler: function() {
									this.delegate.cmOn('onConfigurationServerClearCacheButtonClick');
								}
							})
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.serviceSynchronization,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: CMDBuild.Translation.serviceSynchronization,
								scope: this,

								handler: function() {
									this.delegate.cmOn('onConfigurationServerServiceSynchButtonClick');
								}
							})
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.lockCardsInEdit,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: CMDBuild.Translation.unlockAllCards,
								scope: this,

								handler: function() {
									this.delegate.cmOn('onConfigurationServerUnlockCardsButtonClick');
								}
							})
						]
					}
				]
			});

			this.callParent(arguments);
		}
	});

})();