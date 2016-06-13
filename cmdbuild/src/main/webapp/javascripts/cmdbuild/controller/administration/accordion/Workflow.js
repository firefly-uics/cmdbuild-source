(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Workflow', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.workflow.Workflow'
		],

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
			'accordionWorkflowUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Workflow}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Workflow', {
				delegate: this,
				disabled: !CMDBuild.configuration.workflow.get(CMDBuild.core.constants.Proxy.ENABLED)
			});

			this.cmfg('accordionWorkflowUpdateStore');
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
		accordionWorkflowUpdateStore: function (nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.proxy.workflow.Workflow.readAll({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

					var nodes = [];
					var nodesMap = {};

					// Removes all processes and root class from response
					decodedResponse = Ext.Array.filter(decodedResponse, function (item, i, array) {
						return (
							item[CMDBuild.core.constants.Proxy.TYPE] == CMDBuild.core.constants.Global.getTableTypeProcessClass() // Discard processes
							&& item[CMDBuild.core.constants.Proxy.NAME] != CMDBuild.core.constants.Global.getRootNameWorkflows() // Discard root workflow of all Workflows
						);
					}, this);

					this.view.getStore().getRootNode().removeAll();

					if (!Ext.isEmpty(decodedResponse)) {
						Ext.Array.forEach(decodedResponse, function (classObject, i, allClassObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.accordionIdentifierGet();
							nodeObject['iconCls'] = classObject['superclass'] ? 'cmdb-tree-superprocessclass-icon' : 'cmdb-tree-processclass-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = classObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.accordionBuildId(classObject[CMDBuild.core.constants.Proxy.ID]);
							nodeObject[CMDBuild.core.constants.Proxy.PARENT] = classObject[CMDBuild.core.constants.Proxy.PARENT];
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = classObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodesMap[nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID]] = nodeObject;
						}, this);

						// Builds full standard/simple classes trees
						for (var id in nodesMap) {
							var node = nodesMap[id];

							if (
								!Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])
								&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]])
							) {
								var parentNode = nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];
								parentNode.children = parentNode.children || [];
								parentNode.children.push(node);
								parentNode[CMDBuild.core.constants.Proxy.LEAF] = false;
							} else {
								nodes.push(node);
							}
						}

						if (!Ext.isEmpty(nodes)) {
							this.view.getStore().getRootNode().appendChild(nodes);
							this.view.getStore().sort();
						}
					}

					this.accordionUpdateStore(arguments); // Custom callParent implementation
				}
			});
		}
	});

})();
