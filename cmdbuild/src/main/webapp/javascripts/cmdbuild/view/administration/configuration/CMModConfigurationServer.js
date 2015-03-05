(function() {

	var tr = CMDBuild.Translation.administration.setup.server;

	Ext.define('CMDBuild.view.administration.configuration.CMModConfigurationServer', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Server}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'server',

		frame: true,
		overflowY: 'auto',
		title: tr.title,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					{
						xtype: 'fieldset',
						title: tr.cache_management,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: tr.clear_cache,
								scope: this,

								handler: function() {
									this.delegate.cmOn('onConfigurationServerClearCacheButtonClick');
								}
							})
						]
					},
					{
						xtype: 'fieldset',
						title: tr.servicesync,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: tr.servicesync,
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
								text: CMDBuild.Translation.unlock_all_cards,
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