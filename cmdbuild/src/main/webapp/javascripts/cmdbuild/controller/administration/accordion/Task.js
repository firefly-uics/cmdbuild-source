(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Task', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionDeselect',
			'accordionExpand',
			'accordionFirstSelectableNodeSelect',
			'accordionFirtsSelectableNodeGet',
			'accordionNodeByIdExists',
			'accordionNodeByIdGet',
			'accordionNodeByIdSelect',
			'accordionTaskUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

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
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Task', { delegate: this });

			this.cmfg('accordionTaskUpdateStore');
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Number or String} parameters.nodeIdToSelect
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		accordionTaskUpdateStore: function (parameters) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-taskGroup-icon',
					text: CMDBuild.Translation.administration.tasks.all,
					description: CMDBuild.Translation.administration.tasks.all,
					id: this.accordionBuildId('all'),
					sectionHierarchy: ['all'],
					leaf: false,

					children: [
						{
							cmName: this.accordionIdentifierGet(),
							iconCls: 'cmdb-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							id: this.accordionBuildId('connector'),
							sectionHierarchy: ['connector'],
							leaf: true
						},
						{
							cmName: this.accordionIdentifierGet(),
							iconCls: 'cmdb-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							id: this.accordionBuildId('email'),
							sectionHierarchy: ['email'],
							leaf: true
						},
						{
							cmName: this.accordionIdentifierGet(),
							iconCls: 'cmdb-tree-taskGroup-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							expanded: true,
							id: this.accordionBuildId('event'),
							sectionHierarchy: ['event'],
							leaf: false,

							children: [
								{
									cmName: this.accordionIdentifierGet(),
									iconCls: 'cmdb-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									id: this.accordionBuildId('event_asynchronous'),
									sectionHierarchy: ['event_asynchronous'], // TODO: use double level (event, asynchronous)
									leaf: true
								},
								{
									cmName: this.accordionIdentifierGet(),
									iconCls: 'cmdb-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									id: this.accordionBuildId('event_synchronous'),
									sectionHierarchy: ['event_synchronous'], // TODO: use double level (event, synchronous)
									leaf: true
								}
							]
						},
						{
							cmName: this.accordionIdentifierGet(),
							iconCls: 'cmdb-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							id: this.accordionBuildId('workflow'),
							sectionHierarchy: ['workflow'],
							leaf: true
						},
						{
							cmName: this.accordionIdentifierGet(),
							iconCls: 'cmdb-tree-taskGroup-icon',
							text: CMDBuild.Translation.others,
							description: CMDBuild.Translation.others,
							expanded: true,
							id: this.accordionBuildId('others'),
							sectionHierarchy: ['generic'],
							leaf: false,

							children: [
								{
									cmName: this.accordionIdentifierGet(),
									iconCls: 'cmdb-tree-tasks-icon',
									text: CMDBuild.Translation.sendEmail,
									description: CMDBuild.Translation.sendEmail,
									id: this.accordionBuildId('generic'),
									sectionHierarchy: ['generic'],
									leaf: true
								}
							]
						}
					]
				}
			]);

			this.accordionUpdateStore(arguments); // Custom callParent implementation
		}
	});

})();
