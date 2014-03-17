(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.accordion.CMTasksAccordion', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: tr.title,
		cmName: 'tasks',

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
			this.selectFirstSelectableNode();
		},

		updateStore: function() {
			var root = this.store.getRootNode();

			root.appendChild([
				{
					id: 'all',
					cmName: 'tasks',
					leaf: true,
					text: tr.all
				},
				{
					id: 'email',
					cmName: 'tasks',
					leaf: true,
					text: tr.tasksTypes.email
				},
				{
					id: 'event',
					cmName: 'tasks',
					leaf: true,
					text: tr.tasksTypes.event
				},
				{
					id: 'workflow',
					cmName: 'tasks',
					leaf: true,
					text: tr.tasksTypes.workflow
				}
			]);
		}
	});

})();