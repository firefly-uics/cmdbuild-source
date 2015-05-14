(function() {

	Ext.define('CMDBuild.controller.administration.domain.EnabledClasses', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.model.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Domain}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDomainEnabledClassesAbortButtonClick',
			'onDomainEnabledClassesAddButtonClick',
			'onDomainEnabledClassesModifyButtonClick',
			'onDomainSelected'
		],

		/**
		 * @cfg {Array}
		 */
		managedTreeTypes: ['destination', 'origin'],

		/**
		 * @cfg {CMDBuild.view.administration.domain.enabledClasses.EnabledClassesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.domain.Domain} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.domain.enabledClasses.EnabledClassesView', {
				delegate: this
			});
		},

		/**
		 * @param {Array} disabledClasses
		 * @param {String} type
		 *
		 * @return {Ext.data.TreeStore} treeStore
		 */
		buildClassesStore: function(disabledClasses, type) {
			var treeStore =  Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.Classes.domainsTreePanel',
				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				},
				sorters: [
					{ property: CMDBuild.ServiceProxy.parameter.DESCRIPTION, direction: 'ASC' }
				]
			});

			if (
				Ext.Array.contains(this.managedTreeTypes, type)
				&& !Ext.isEmpty(this.cmfg('selectedDomainGet'))
			) {
				var root = treeStore.getRootNode();
				var standard = [];
				var rootId = undefined;

				root.removeAll();

				// GetAllClasses data to get default translations
				CMDBuild.core.proxy.Classes.read({
					params: {
						active: true
					},
					scope: this,
					success: function(response, options, decodedResponse) {
						var nodesMap = {};

						Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
							if (
								classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
								&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
								&& classObject['tableType'] == 'standard' // Discard simple classes
							) {
								// Class node object
								var classMainNodeObject = {};
								classMainNodeObject['iconCls'] = classObject['superclass'] ? 'cmdbuild-tree-superclass-icon' : 'cmdbuild-tree-class-icon';
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENABLED] = !Ext.Array.contains(disabledClasses, classObject[CMDBuild.core.proxy.CMProxyConstants.NAME]);
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.ID] = classObject[CMDBuild.core.proxy.CMProxyConstants.ID];
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classObject[CMDBuild.core.proxy.CMProxyConstants.PARENT];

								nodesMap[classMainNodeObject.id] = classMainNodeObject;
							}
						}, this);

						// Builds full standard/simple classes trees
						for (var id in nodesMap) {
							var node = nodesMap[id];

							if (!Ext.isEmpty(node[CMDBuild.core.proxy.CMProxyConstants.PARENT]) && !Ext.isEmpty(nodesMap[node[CMDBuild.core.proxy.CMProxyConstants.PARENT]])) {
								var parentNode = nodesMap[node[CMDBuild.core.proxy.CMProxyConstants.PARENT]];
								parentNode.children = parentNode.children || [];
								parentNode.children.push(node);
								parentNode[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							} else {
								standard.push(node);
							}
						}

						// Get root node and build offspring tree
						switch(type) {
							case 'destination': {
								rootId = this.cmfg('selectedDomainGet').get('idClass2');
							} break;

							case 'origin': {
								rootId = this.cmfg('selectedDomainGet').get('idClass1');
							} break;
						}

						if (!Ext.isEmpty(nodesMap[rootId]))
							root.appendChild(nodesMap[rootId]);
					},
					callback: function(records, operation, success) {
						this.view.originTree.expandAll();
						this.view.destinationTree.expandAll();
					}
				});
			}

			return treeStore;
		},

		/**
		 * @param {Object} node
		 * @param {Array} destinationArray
		 */
		getEnabledTreeVisit: function(node, destinationArray) {
			node.eachChild(function(childNode) {
				if (!childNode.get(CMDBuild.core.proxy.CMProxyConstants.ENABLED))
					destinationArray.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));

				if (!Ext.isEmpty(node.hasChildNodes()))
					this.getEnabledTreeVisit(childNode, destinationArray);
			}, this);
		},

		onDomainEnabledClassesAbortButtonClick: function() {
			this.view.buildTrees();

			this.view.setDisabledModify(true, true, true);
		},

		onDomainEnabledClassesAddButtonClick: function() {
			this.view.disable();
		},

		onDomainEnabledClassesModifyButtonClick: function() {
			this.view.setDisabledModify(false);
		},

		onDomainSelected: function() {
			this.view.buildTrees();
			this.view.enable();
			this.view.setDisabledModify(true);
		}
	});

})();