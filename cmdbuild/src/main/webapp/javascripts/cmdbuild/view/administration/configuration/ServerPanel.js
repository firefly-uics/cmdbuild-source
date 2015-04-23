(function() {

	Ext.define('CMDBuild.view.administration.configuration.ServerPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Server}
		 */
		delegate: undefined,

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
									this.delegate.cmOn('onServerClearCacheButtonClick');
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
									this.delegate.cmOn('onServerServiceSynchButtonClick');
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
									this.delegate.cmOn('onServerUnlockCardsButtonClick');
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