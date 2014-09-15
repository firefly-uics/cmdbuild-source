(function() {

	var tr = CMDBuild.Translation.administration.email;

	Ext.define('CMDBuild.view.administration.accordion.CMAccordionEmail', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		cmName: 'email',
		title: tr.title,

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		/**
		 * @overwrite
		 */
		updateStore: function() {
			this.store.getRootNode().appendChild([
				{
					text: tr.accounts.title,
					leaf: true,
					cmName: 'emailAccounts'
				},
				{
					text: tr.templates.title,
					leaf: true,
					cmName: 'emailTemplates'
				}
			]);
		}
	});

})();