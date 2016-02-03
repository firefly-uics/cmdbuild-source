(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Task', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Task}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Task', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function(nodeIdToSelect) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdbuild-tree-tasks-group-icon',
					text: CMDBuild.Translation.administration.tasks.all,
					description: CMDBuild.Translation.administration.tasks.all,
					id: this.cmfg('accordionBuildId', { components: 'all' }),
					sectionHierarchy: ['all'],
					leaf: false,

					children: [
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdbuild-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							id: this.cmfg('accordionBuildId', { components: 'connector' }),
							sectionHierarchy: ['connector'],
							leaf: true
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdbuild-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							id: this.cmfg('accordionBuildId', { components: 'email' }),
							sectionHierarchy: ['email'],
							leaf: true
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdbuild-tree-tasks-group-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							expanded: true,
							id: this.cmfg('accordionBuildId', { components: 'event' }),
							sectionHierarchy: ['event'],
							leaf: false,

							children: [
								{
									cmName: this.cmfg('accordionIdentifierGet'),
									iconCls: 'cmdbuild-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									id: this.cmfg('accordionBuildId', { components: 'event_asynchronous' }),
									sectionHierarchy: ['event_asynchronous'], // TODO: use double level (event, asynchronous)
									leaf: true
								},
								{
									cmName: this.cmfg('accordionIdentifierGet'),
									iconCls: 'cmdbuild-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									id: this.cmfg('accordionBuildId', { components: 'event_synchronous' }),
									sectionHierarchy: ['event_synchronous'], // TODO: use double level (event, synchronous)
									leaf: true
								}
							]
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdbuild-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							id: this.cmfg('accordionBuildId', { components: 'workflow' }),
							sectionHierarchy: ['workflow'],
							leaf: true
						}
					]
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();