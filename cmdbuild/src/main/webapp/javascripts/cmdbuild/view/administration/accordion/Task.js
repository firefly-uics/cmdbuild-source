(function() {

	Ext.define('CMDBuild.view.administration.accordion.Task', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
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
					cmName: this.cmName,
					iconCls: 'cmdbuild-tree-tasks-group-icon',
					text: CMDBuild.Translation.administration.tasks.all,
					description: CMDBuild.Translation.administration.tasks.all,
					id: this.delegate.cmfg('accordionBuildId', { components: 'all' }),
					sectionHierarchy: ['all'],
					leaf: false,

					children: [
						{
							cmName: this.cmName,
							iconCls: 'cmdbuild-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							id: this.delegate.cmfg('accordionBuildId', { components: 'connector' }),
							sectionHierarchy: ['connector'],
							leaf: true
						},
						{
							cmName: this.cmName,
							iconCls: 'cmdbuild-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							id: this.delegate.cmfg('accordionBuildId', { components: 'email' }),
							sectionHierarchy: ['email'],
							leaf: true
						},
						{
							cmName: this.cmName,
							iconCls: 'cmdbuild-tree-tasks-group-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							expanded: true,
							id: this.delegate.cmfg('accordionBuildId', { components: 'event' }),
							sectionHierarchy: ['event'],
							leaf: false,

							children: [
								{
									cmName: this.cmName,
									iconCls: 'cmdbuild-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									id: this.delegate.cmfg('accordionBuildId', { components: 'event_asynchronous' }),
									sectionHierarchy: ['event_asynchronous'], // TODO: use double level (event, asynchronous)
									leaf: true
								},
								{
									cmName: this.cmName,
									iconCls: 'cmdbuild-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									id: this.delegate.cmfg('accordionBuildId', { components: 'event_synchronous' }),
									sectionHierarchy: ['event_synchronous'], // TODO: use double level (event, synchronous)
									leaf: true
								}
							]
						},
						{
							cmName: this.cmName,
							iconCls: 'cmdbuild-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							id: this.delegate.cmfg('accordionBuildId', { components: 'workflow' }),
							sectionHierarchy: ['workflow'],
							leaf: true
						}
					]
				}
			]);

			this.callParent(arguments);
		}
	});

})();