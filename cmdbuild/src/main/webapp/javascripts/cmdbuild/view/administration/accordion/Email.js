(function() {

	Ext.define('CMDBuild.view.administration.accordion.Email', {
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
					id: 'accounts',
					cmName: 'email',
					leaf: true,
					text: CMDBuild.Translation.accounts
				},
				{
					id: 'templates',
					cmName: 'email',
					leaf: true,
					text: CMDBuild.Translation.templates,
				},
				{
					id: 'queue',
					cmName: 'email',
					leaf: true,
					text: CMDBuild.Translation.queue,
				}
			]);
		}
	});

})();