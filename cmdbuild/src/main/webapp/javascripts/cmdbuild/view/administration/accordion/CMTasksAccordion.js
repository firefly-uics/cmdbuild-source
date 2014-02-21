(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.accordion.CMTasksAccordion', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: tr.title,
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
					text: tr.all,
					type: 'all'
				},
				{
					id: '2',
					cmName: 'tasks',
					leaf: true,
					text: tr.tasksTypes.email,
					type:'email'
				},
				{
					id: '3',
					cmName: 'tasks',
					leaf: true,
					text: tr.tasksTypes.event,
					type:'event'
				},
				{
					id: '4',
					cmName: 'tasks',
					leaf: true,
					text: tr.tasksTypes.workflow,
					type:'workflow'
				}
			]);
		}
	});

})();