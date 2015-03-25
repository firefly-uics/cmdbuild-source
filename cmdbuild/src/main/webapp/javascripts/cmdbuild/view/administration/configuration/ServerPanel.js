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

		bodyCls: 'cmgraypanel',
		border: false,
		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			Ext.apply(this, {
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
									this.delegate.cmOn('onConfigurationClearCacheButtonClick');
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
									this.delegate.cmOn('onConfigurationServiceSynchButtonClick');
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
									this.delegate.cmOn('onConfigurationUnlockCardsButtonClick');
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