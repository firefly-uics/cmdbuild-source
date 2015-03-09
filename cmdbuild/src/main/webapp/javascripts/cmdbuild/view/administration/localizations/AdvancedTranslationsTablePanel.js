(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
		 */
		delegate: undefined,

		activeTab: 0,
		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		buttonAlign: 'center',
		frame: false,
		region: 'center',

		initComponent: function() {
			Ext.apply(this, {
				buttons: [
					Ext.create('CMDBuild.buttons.SaveButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onAdvancedTableSaveButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onAdvancedTableAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();