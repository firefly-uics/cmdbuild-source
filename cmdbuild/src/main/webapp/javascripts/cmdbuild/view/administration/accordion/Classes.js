(function() {

	Ext.define('CMDBuild.view.administration.accordion.Classes', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.model.common.accordion.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Classes}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		delegateClassName: 'CMDBuild.controller.administration.accordion.Classes',

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Classes',

		title: CMDBuild.Translation.classList,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.Classes.readAll({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

					var nodes = [];
					var standard = [];
					var simple = [];
					var standardNodesMap = {};

					// Removes all processes and root class from response
					decodedResponse = Ext.Array.filter(decodedResponse, function(item, i, array) {
						return (
							item[CMDBuild.core.constants.Proxy.TYPE] != CMDBuild.core.constants.Global.getTableTypeProcessClass() // Discard processes
							&& item[CMDBuild.core.constants.Proxy.NAME] != 'Class' // Discard root class of all classes
						);
					}, this);

					if (!Ext.isEmpty(decodedResponse)) {
						Ext.Array.forEach(decodedResponse, function(classObject, i, allClassObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmName;
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = classObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', classObject[CMDBuild.core.constants.Proxy.ID]);
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = classObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							if (classObject[CMDBuild.core.constants.Proxy.TABLE_TYPE] == CMDBuild.core.constants.Global.getTableTypeSimpleTable()) {
								nodeObject['iconCls'] = 'cmdbuild-tree-class-icon';

								simple.push(nodeObject);
							} else { // Standard nodes map build
								nodeObject['iconCls'] = classObject['superclass'] ? 'cmdbuild-tree-superclass-icon' : 'cmdbuild-tree-class-icon';
								nodeObject[CMDBuild.core.constants.Proxy.PARENT] = classObject[CMDBuild.core.constants.Proxy.PARENT];

								standardNodesMap[nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID]] = nodeObject;
							}
						}, this);

						// Builds full standard/simple classes trees
						for (var id in standardNodesMap) {
							var node = standardNodesMap[id];

							if (
								!Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])
								&& !Ext.isEmpty(standardNodesMap[node[CMDBuild.core.constants.Proxy.PARENT]])
							) {
								var parentNode = standardNodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];
								parentNode.children = parentNode.children || [];
								parentNode.children.push(node);
								parentNode[CMDBuild.core.constants.Proxy.LEAF] = false;
							} else {
								standard.push(node);
							}
						}

						if (Ext.isEmpty(simple)) {
							nodes = standard;
						} else {
							nodes = [
								{
									iconCls: 'cmdbuild-tree-superclass-icon',
									text: CMDBuild.Translation.standard,
									description: CMDBuild.Translation.standard,
									entityId: null, // To be unselectable
									children: standard,
									expanded: true,
									leaf: false
								},
								{
									iconCls: 'cmdbuild-tree-superclass-icon',
									text: CMDBuild.Translation.simple,
									description: CMDBuild.Translation.simple,
									entityId: null, // To be unselectable
									children: simple,
									expanded: true,
									leaf: false,
								}
							];
						}

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						if (!Ext.isEmpty(this.delegate))
							this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
				}
			});
		}
	});

})();