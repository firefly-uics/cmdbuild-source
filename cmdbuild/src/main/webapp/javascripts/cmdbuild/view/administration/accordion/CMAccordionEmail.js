(function() {

	Ext.define('CMDBuild.view.administration.accordion.CMAccordionEmail', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		cmName: 'email',
		title: CMDBuild.Translation.email,

		constructor: function(){
			this.callParent(arguments);

			this.updateStore();
		},

		/**
		 * @override
		 */
		updateStore: function() {
			this.store.getRootNode().appendChild([
				{
					text: CMDBuild.Translation.accounts,
					leaf: true,
					cmName: 'emailAccounts'
				},
				{
					text: CMDBuild.Translation.templates,
					leaf: true,
					cmName: 'emailTemplates'
				}
			]);
		}
	});

})();