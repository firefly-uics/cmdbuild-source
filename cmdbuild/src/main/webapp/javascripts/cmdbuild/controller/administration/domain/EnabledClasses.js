(function() {

	Ext.define('CMDBuild.controller.administration.domain.EnabledClasses', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.CMModDomainController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		managedTreeTypes: ['destination', 'origin'],

		/**
		 * @cfg {CMDBuild.view.administration.domain.enabledClasses.MainPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.domain.CMModDomainController} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.domain.enabledClasses.MainPanel', {
				delegate: this
			});
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
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
				sorters: [{
					property: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					direction: 'ASC'
				}]
			});

			if (
				Ext.Array.contains(this.managedTreeTypes, type)
				&& !Ext.isEmpty(this.getSelectedDomain())
			) {
				var root = treeStore.getRootNode();
				var standard = [];
				var rootData = {};

				root.removeAll();

				// GetAllClasses data to get default translations
				CMDBuild.ServiceProxy.classes.read({
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
								&& classObject[CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE] == 'standard' // Discard simple classes
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

							if (
								!Ext.isEmpty(node[CMDBuild.core.proxy.CMProxyConstants.PARENT])
								&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.proxy.CMProxyConstants.PARENT]])
							) {
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
								rootData[CMDBuild.core.proxy.CMProxyConstants.ID] = this.getSelectedDomain().get('idClass2');
								rootData[CMDBuild.core.proxy.CMProxyConstants.NAME] = this.getSelectedDomain().get('nameClass2');
							} break;

							case 'origin': {
								rootData[CMDBuild.core.proxy.CMProxyConstants.ID] = this.getSelectedDomain().get('idClass1');
								rootData[CMDBuild.core.proxy.CMProxyConstants.NAME] = this.getSelectedDomain().get('nameClass1');
							} break;
						}

						if (!Ext.isEmpty(nodesMap[rootData.id])) { // Node is class
							root.appendChild(nodesMap[rootData.id]);
						} else { // Node is process so build custom node
							var customNodeObject = {};
							customNodeObject['iconCls'] = 'cmdbuild-tree-processclass-icon';
							customNodeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = rootData[CMDBuild.core.proxy.CMProxyConstants.NAME];
							customNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENABLED] = true;
							customNodeObject[CMDBuild.core.proxy.CMProxyConstants.ID] = rootData[CMDBuild.core.proxy.CMProxyConstants.ID];
							customNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							customNodeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] = rootData[CMDBuild.core.proxy.CMProxyConstants.NAME];

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

		/**
		 * @return {CMDBuild.cache.CMDomainModel} or null
		 */
		getSelectedDomain: function() {
			if (Ext.isEmpty(this.parentDelegate.selectedDomain))
				return null;

			return this.parentDelegate.selectedDomain;
		},

		/**
		 * @return {CMDBuild.view.administration.domain.enabledClasses.MainPanel}
		 */
		getView: function() {
			return this.view;
		},

		onAbortButtonClick: function() {
			this.view.buildTrees();

			this.setDisabled(true);
		},

		onAddButtonClick: function() {
			this.view.setDisabled(true);
		},

		onDomainSelected: function() {
			this.view.buildTrees();
			this.view.setDisabled(false);

			this.setDisabled(true);
		},

		onModifyButtonClick: function() {
			this.setDisabled(false);
		},

		/**
		 * Global setDisabled
		 *
		 * @param {Boolean} state
		 *
		 * @private
		 *
		 * TODO: to delete when CMFormCunctions will be refactored
		 */
		setDisabled: function(state) {
			var topToolbar = this.view.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP);
			var bottomToolbar = this.view.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM);

			this.view.originTree.setDisabled(state);
			this.view.destinationTree.setDisabled(state);

			Ext.Array.forEach(topToolbar.items.items, function(button, i, allButtons) {
				button.setDisabled(!state);
			}, this);

			Ext.Array.forEach(bottomToolbar.items.items, function(button, i, allButtons) {
				button.setDisabled(state);
			}, this);
		}
	});

})();