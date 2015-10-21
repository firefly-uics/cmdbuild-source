(function() {

	Ext.define('CMDBuild.view.administration.accordion.Task', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Task}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.administration.tasks.title,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					text: CMDBuild.Translation.administration.tasks.all,
					iconCls: 'cmdbuild-tree-tasks-group-icon',
					cmName: this.cmName,
					sectionHierarchy: ['all'],
					leaf: false,
					children: [
						{
							text: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							iconCls: 'cmdbuild-tree-tasks-icon',
							cmName: this.cmName,
							sectionHierarchy: ['connector'],
							leaf: true,
						},
						{
							text: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							iconCls: 'cmdbuild-tree-tasks-icon',
							cmName: this.cmName,
							sectionHierarchy: ['email'],
							leaf: true,
						},
						{
							text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							iconCls: 'cmdbuild-tree-tasks-group-icon',
							cmName: this.cmName,
							sectionHierarchy: ['event'],
							expanded: true,
							leaf: false,
							children: [
								{
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									iconCls: 'cmdbuild-tree-tasks-icon',
									cmName: this.cmName,
									sectionHierarchy: ['event_asynchronous'], // TODO: use double level (event, asynchronous)
									leaf: true,
								},
								{
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									iconCls: 'cmdbuild-tree-tasks-icon',
									cmName: this.cmName,
									sectionHierarchy: ['event_synchronous'], // TODO: use double level (event, synchronous)
									leaf: true,
								}
							]
						},
						{
							text: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							iconCls: 'cmdbuild-tree-tasks-icon',
							cmName: this.cmName,
							sectionHierarchy: ['workflow'],
							leaf: true,
						}
					]
				}
			]);

			this.callParent(arguments);
		}
	});

})();