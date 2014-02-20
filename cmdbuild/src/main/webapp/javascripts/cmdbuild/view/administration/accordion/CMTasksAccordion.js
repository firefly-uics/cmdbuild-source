(function() {

	Ext.define('CMDBuild.view.administration.accordion.CMTasksAccordion', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: '@@ Tasks',
		cmName: 'tasks',

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();

			root.appendChild([
				{
					id: '1',
					cmName: 'tasks',
					leaf: true,
					text: '@@ All',
					type: 'all'
				},
				{
					id: '2',
					cmName: 'tasks',
					leaf: true,
					text: '@@ Email',
					type:'email'
				},
				{
					id: '3',
					cmName: 'tasks',
					leaf: true,
					text: '@@ Event',
					type:'event'
				},
				{
					id: '4',
					cmName: 'tasks',
					leaf: true,
					text: '@@ Workflow',
					type:'workflow'
				}
			]);
		}
	});

})();