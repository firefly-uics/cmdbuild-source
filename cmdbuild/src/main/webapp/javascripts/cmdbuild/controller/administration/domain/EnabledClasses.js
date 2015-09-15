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
			'onDomainEnabledClassesAbortButtonClick',
			'onDomainEnabledClassesAddButtonClick',
			'onDomainEnabledClassesDomainSelected = onDomainSelected',
			'onDomainEnabledClassesModifyButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.domain.enabledClasses.TreePanel}
		 */
		destinationTree: undefined,

		/**
		 * @cfg {Array}
		 */
		managedTreeTypes: ['destination', 'origin'],

		/**
		 * @property {CMDBuild.view.administration.domain.enabledClasses.TreePanel}
		 */
		originTree: undefined,

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

			this.view = Ext.create('CMDBuild.view.administration.domain.enabledClasses.EnabledClassesView', { delegate: this });

			// Shorthands
			this.destinationTree = this.view.destinationTree;
			this.originTree = this.view.originTree;
		},

		/**
		 * @param {String} type
		 */
		fillTreeStore: function(type) {
			if (
				Ext.Array.contains(this.managedTreeTypes, type)
				&& !this.cmfg('domainSelectedDomainIsEmpty')
			) {
				var disabledClasses = [];
				var root = undefined;
				var rootData = {};
				var standard = [];

				// Get tree configurations by type
				switch(type) {
					case CMDBuild.core.proxy.Constants.DESTINATION: {
						root = this.destinationTree.getStore().getRootNode();
						disabledClasses = this.cmfg('domainSelectedDomainGet', CMDBuild.core.proxy.Constants.DESTINATION_DISABLED_CLASSES);

						rootData[CMDBuild.core.proxy.Constants.ID] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.proxy.Constants.DESTINATION_CLASS_ID);
						rootData[CMDBuild.core.proxy.Constants.NAME] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.proxy.Constants.DESTINATION_CLASS_NAME);
					} break;

					case CMDBuild.core.proxy.Constants.ORIGIN: {
						root = this.originTree.getStore().getRootNode();
						disabledClasses = this.cmfg('domainSelectedDomainGet', CMDBuild.core.proxy.Constants.ORIGIN_DISABLED_CLASSES);

						rootData[CMDBuild.core.proxy.Constants.ID] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.proxy.Constants.ORIGIN_CLASS_ID);
						rootData[CMDBuild.core.proxy.Constants.NAME] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.proxy.Constants.ORIGIN_CLASS_NAME);
					} break;
				}

				root.removeAll();

				var params = {};
				params[CMDBuild.core.proxy.Constants.ACTIVE] = true;

				// GetAllClasses data to get default translations
				CMDBuild.core.proxy.Classes.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						var nodesMap = {};

						Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
							if (
								classObject[CMDBuild.core.proxy.Constants.TYPE] == 'class' // Discard processes from visualization
								&& classObject[CMDBuild.core.proxy.Constants.NAME] != 'Class' // Discard root class of all classes
								&& classObject[CMDBuild.core.proxy.Constants.TABLE_TYPE] != CMDBuild.Constants.cachedTableType.simpletable // Discard simple classes
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

								nodesMap[classMainNodeObject[CMDBuild.core.proxy.Constants.ID]] = classMainNodeObject;
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

						// Build offspring tree
						if (!Ext.isEmpty(nodesMap[rootData[CMDBuild.core.proxy.Constants.ID]])) { // Node is class
							root.appendChild(nodesMap[rootData[CMDBuild.core.proxy.Constants.ID]]);
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
						this.originTree.expandAll();
						this.destinationTree.expandAll();
					}
				});
			}
		},

		/**
		 * @returns {Object} data
		 *
		 * @public
		 */
		getData: function() {
			var data = {};
			var destinationDisabledClasses = [];
			var originDisabledClasses = [];

			// Get origin disabled classes
			this.getDisabledTreeVisit({
				node: this.originTree.getStore().getRootNode(),
				destinationArray: originDisabledClasses
			});
			data['disabled1'] = Ext.encode(originDisabledClasses);

			// Get destination disabled classes
			this.getDisabledTreeVisit({
				node: this.destinationTree.getStore().getRootNode(),
				destinationArray: destinationDisabledClasses
			});
			data['disabled2'] = Ext.encode(destinationDisabledClasses);

			return data;
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
			if (this.cmfg('domainSelectedDomainIsEmpty')) {
				this.view.reset();
				this.view.setDisabledModify(true, true, true);
			} else {
				this.onDomainEnabledClassesDomainSelected();
			}
		},

		onDomainEnabledClassesAddButtonClick: function() {
			this.view.disable();
		},

		onDomainEnabledClassesDomainSelected: function() {
			this.fillTreeStore(CMDBuild.core.proxy.Constants.DESTINATION);
			this.fillTreeStore(CMDBuild.core.proxy.Constants.ORIGIN);

			this.view.enable();
			this.view.setDisabledModify(true);
		},

		onDomainEnabledClassesModifyButtonClick: function() {
			this.view.setDisabledModify(false);
		}
	});

})();