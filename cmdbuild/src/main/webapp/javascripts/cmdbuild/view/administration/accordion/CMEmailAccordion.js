(function() {

	var tr = CMDBuild.Translation.administration.setup.email; // Path to translation

	Ext.define("CMDBuild.view.administration.accordion.CMEmailAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",

		title: tr.title,
		cmName: 'email',

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();

			root.appendChild([
				{
					text: tr.accounts.title,
					leaf: true,
					cmName: 'setupEmailAccounts'
				},
				{
					text: tr.templates.title,
					leaf: true,
					cmName: 'setupEmailTemplates'
				}
			]);
		}
	});

})();