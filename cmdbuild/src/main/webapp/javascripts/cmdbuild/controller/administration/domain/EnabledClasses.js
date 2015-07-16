(function() {

	Ext.define('CMDBuild.controller.administration.domain.EnabledClasses', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
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
			'getDisabledTreeVisit',
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
				var rootData = {};

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
								classObject[CMDBuild.core.proxy.Constants.TYPE] == 'class' // Discard processes from visualization
								&& classObject[CMDBuild.core.proxy.Constants.NAME] != 'Class' // Discard root class of all classes
								&& classObject[CMDBuild.core.proxy.Constants.TABLE_TYPE] == 'standard' // Discard simple classes
							) {
								// Class node object
								var classMainNodeObject = {};
								classMainNodeObject['iconCls'] = classObject['superclass'] ? 'cmdbuild-tree-superclass-icon' : 'cmdbuild-tree-class-icon';
								classMainNodeObject[CMDBuild.core.proxy.Constants.DESCRIPTION] = classObject[CMDBuild.core.proxy.Constants.TEXT];
								classMainNodeObject[CMDBuild.core.proxy.Constants.ENABLED] = !Ext.Array.contains(disabledClasses, classObject[CMDBuild.core.proxy.Constants.NAME]);
								classMainNodeObject[CMDBuild.core.proxy.Constants.ID] = classObject[CMDBuild.core.proxy.Constants.ID];
								classMainNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
								classMainNodeObject[CMDBuild.core.proxy.Constants.NAME] = classObject[CMDBuild.core.proxy.Constants.NAME];
								classMainNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classObject[CMDBuild.core.proxy.Constants.PARENT];

								nodesMap[classMainNodeObject.id] = classMainNodeObject;
							}
						}, this);

						// Builds full standard/simple classes trees
						for (var id in nodesMap) {
							var node = nodesMap[id];

							if (
								!Ext.isEmpty(node[CMDBuild.core.proxy.Constants.PARENT])
								&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]])
							) {
								var parentNode = nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]];
								parentNode.children = parentNode.children || [];
								parentNode.children.push(node);
								parentNode[CMDBuild.core.proxy.Constants.LEAF] = false;
							} else {
								standard.push(node);
							}
						}

						// Get root node and build offspring tree
						switch(type) {
							case 'destination': {
								rootData[CMDBuild.core.proxy.Constants.ID] = this.cmfg('selectedDomainGet').get('idClass2');
								rootData[CMDBuild.core.proxy.Constants.NAME] = this.cmfg('selectedDomainGet').get('nameClass2');
							} break;

							case 'origin': {
								rootData[CMDBuild.core.proxy.Constants.ID] = this.cmfg('selectedDomainGet').get('idClass1');
								rootData[CMDBuild.core.proxy.Constants.NAME] = this.cmfg('selectedDomainGet').get('nameClass1');
							} break;
						}

						if (!Ext.isEmpty(nodesMap[rootData.id])) { // Node is class
							root.appendChild(nodesMap[rootData.id]);
						} else { // Node is process so build custom node
							var customNodeObject = {};
							customNodeObject['iconCls'] = 'cmdbuild-tree-processclass-icon';
							customNodeObject[CMDBuild.core.proxy.Constants.DESCRIPTION] = rootData[CMDBuild.core.proxy.Constants.NAME];
							customNodeObject[CMDBuild.core.proxy.Constants.ENABLED] = true;
							customNodeObject[CMDBuild.core.proxy.Constants.ID] = rootData[CMDBuild.core.proxy.Constants.ID];
							customNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
							customNodeObject[CMDBuild.core.proxy.Constants.NAME] = rootData[CMDBuild.core.proxy.Constants.NAME];

							root.appendChild(customNodeObject);
						}
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
		 * @param {Object} parametersObject
		 * @param {Object} parametersObject.node
		 * @param {Array} parametersObject.destinationArray
		 */
		getDisabledTreeVisit: function(parametersObject) {
			if (
				!Ext.isEmpty(parametersObject)
				&& !Ext.isEmpty(parametersObject.node)
				&& Ext.isArray(parametersObject.destinationArray)
			) {
				var node = parametersObject.node;
				var destinationArray = parametersObject.destinationArray;

				node.eachChild(function(childNode) {
					if (!childNode.hasChildNodes() && !childNode.get(CMDBuild.core.proxy.Constants.ENABLED))
						destinationArray.push(childNode.get(CMDBuild.core.proxy.Constants.NAME));

					if (node.hasChildNodes())
						this.getDisabledTreeVisit({
							node: childNode,
							destinationArray: destinationArray
						});
				}, this);
			} else {
				_error('wrong getDisabledTreeVisit parametersObject', this);
			}
		},

		onDomainEnabledClassesAbortButtonClick: function() {
			if (Ext.isEmpty(this.cmfg('selectedDomainGet'))) {
				this.view.reset();
				this.view.setDisabledModify(true, true, true);
			} else {
				this.onDomainSelected(this.cmfg('selectedDomainGet'));
			}
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