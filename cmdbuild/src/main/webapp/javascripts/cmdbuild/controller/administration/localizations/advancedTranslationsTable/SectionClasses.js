(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTranslationsTable.SectionClasses', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTranslationsTable.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: 'classes',

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.advancedTranslationsTable.Main} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTranslationsTable.SectionClassesPanel', {
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
				case 'buildStore':
					return this.buildStore();

				case 'onAdvancedTableNodeExpand':
					return this.onAdvancedTableNodeExpand(param);

				case 'onAdvancedTableRowUpdateButtonClick':
					return this.onAdvancedTableRowUpdateButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {Ext.data.TreeStore} treeStore
		 */
		buildStore: function() {
_debug('CMDBuild.Config', CMDBuild.Config);
			var treeStore =  Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.Localizations.sectionClassesTreeStore',
				folderSort: true,
				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				}
			});
			var root = treeStore.getRootNode();

			// GetAllClasses data to get default translations
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.Classes.read({
				params: {
					active: true
				},
				scope: this,
				success: function(response, options, decodedResponse) {
_debug('Classes decodedResponse', decodedResponse);
					Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
						if (
							classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
							&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
						) {
							// Class main node
							var classMainNodeObject = { expandable: true, };
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = root;
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = 'classes'; // TODO proxy costants

							var classMainNode = root.appendChild(classMainNodeObject);

							// Class description property object
							var classDescriptionNodeObject = {};
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Description';
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classMainNode;
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;

							classMainNode.appendChild(classDescriptionNodeObject);

							// Class attributes node
							var classAttributeNodeObject = {
								expandable: true,
								parent: classMainNode
							};
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Attributes';
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classMainNode;
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES;

							var classAttributesNode = classMainNode.appendChild(classAttributeNodeObject);

							classAttributesNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make attributes node expandable
						}
					}, this);
				},
				callback: function(records, operation, success) {
					CMDBuild.LoadMask.get().hide();
				}
			});

			return treeStore;
		},

		/**
		 * @param {CMDBuild.model.Localizations.sectionClassesTreeStore} startNode
		 *
		 * @return {CMDBuild.model.Localizations.sectionClassesTreeStore} classNode
		 */
		getClassNode: function(node) {
_debug('node1', node);
			while (node.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY) != 'classes') { // TODO proxy costants
_debug('node loop', node);
				node = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT);
			}
_debug('node2', node);
			return node;
		},

		/**
		 * @return {String}
		 */
		getSectionId: function() {
			return this.sectionId;
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * Refresh all child node filling them with translations (class description)
		 *
		 * @param {CMDBuild.model.Localizations.sectionClassesTreeStore} node
		 */
		nodeExpandLevel1: function(node) {
_debug('nodeExpandLevel1 node', node);
			CMDBuild.LoadMask.get().show();
			node.eachChild(function(childNode) {
				if (childNode.isLeaf()) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
					params['field'] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY); // TODO: proxyCostants
					params['sectionId'] = this.getSectionId(); // TODO proxy costants and get sectionId

					CMDBuild.core.proxy.Localizations.readLocalization({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							// Class property object
							var classPropertyNodeObject = {};
							classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.DEFAULT);
							classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
							classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = node;
							classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);
_debug('nodeExpandLevel1 decodedResponse', decodedResponse);
							if (!Ext.Object.isEmpty(decodedResponse.response))
								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
									classPropertyNodeObject[tag] = translation;
								});

							node.replaceChild(classPropertyNodeObject, childNode);
						},
						callback: function(records, operation, success) {
							CMDBuild.LoadMask.get().hide();
						}
					});
				}
			}, this);
		},

		/**
		 * Rebuild all child node with translations (class attributes)
		 *
		 * @param {CMDBuild.model.Localizations.sectionClassesTreeStore} node
		 */
		nodeExpandLevel2: function(node) {
_debug('nodeExpandLevel2', node);
			node.removeAll();

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.Attributes.read({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
_debug('nodeExpandLevel2 Attributes decodedResponse', decodedResponse);
					Ext.Array.forEach(decodedResponse.attributes, function(attributeObject, index, allAttributes) {
						if (attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Notes') { // Custom CMDBuild behaviour
							var localizationParams = {};
							localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
							localizationParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
							localizationParams['field'] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION; // TODO: proxyCostants
							localizationParams['sectionId'] = this.getSectionId() + CMDBuild.core.Utils.toTitleCase(node.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY)); // TODO proxy costants and get sectionId

							CMDBuild.core.proxy.Localizations.readLocalization({
								params: localizationParams,
								scope: this,
								loadMask: true,
								success: function(response, options, decodedResponse) {
_debug('nodeExpandLevel2 localizations decodedResponse', decodedResponse);
									var childAttributeNodeObject = {};
									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = node;
									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE;
									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];

									if (!Ext.Object.isEmpty(decodedResponse.response)) {
										Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
											childAttributeNodeObject[tag] = translation;
										});

										childAttributeNodeObject['wasEmpty'] = false;
									}

									node.appendChild(childAttributeNodeObject);
								}
							});
						}
					}, this);
				},
				callback: function(records, operation, success) {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		/**
		 * @param {CMDBuild.model.Localizations.sectionClassesTreeStore} node
		 */
		onAdvancedTableNodeExpand: function(node) {
_debug('onAdvancedTableNodeExpand', node);
			if (!Ext.isEmpty(node) && node.getDepth() == 1) { // First level node (class node)
				this.nodeExpandLevel1(node);
			} else if (!Ext.isEmpty(node) && node.getDepth() == 2) { // Second level node (attributes node)
				this.nodeExpandLevel2(node);
			}
		},

		/**
		 * @param {CMDBuild.model.Localizations.sectionClassesTreeStore} node
		 */
		onAdvancedTableRowUpdateButtonClick: function(node) {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable UPDATE', node);
			// TODO fare chiamata per salvataggio traduzioni a partire dai dati del node
			if (!Ext.Object.isEmpty(node)) {
				var parentProperty = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);

				var localizationParams = {};
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.getClassNode(node).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
				localizationParams['field'] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION; // TODO: proxyCostants
				localizationParams['attributeName'] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT); // TODO: proxyCostants
				localizationParams['translations'] = Ext.encode(node.getChanges()); // TODO: proxyCostants
				localizationParams['sectionId'] = (parentProperty == 'classes') ? this.getSectionId() : this.getSectionId() + CMDBuild.core.Utils.toTitleCase(parentProperty); // TODO proxy costants and get sectionId
_debug('node wasEmpty', node.get('wasEmpty'));
				if (node.get('wasEmpty')) {
					CMDBuild.core.proxy.Localizations.createLocalization({
						params: localizationParams,
						scope: this,
						loadMask: true,
						success: function(response, options, decodedResponse) {
_debug('nodeExpandLevel2 localizations decodedResponse', decodedResponse);
//							var childAttributeNodeObject = {};
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = node;
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE;
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
//
//							if (!Ext.Object.isEmpty(decodedResponse.response))
//								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
//									childAttributeNodeObject[tag] = translation;
//								});
//
//							node.appendChild(childAttributeNodeObject);
						}
					});
				} else {
					CMDBuild.core.proxy.Localizations.updateLocalization({
						params: localizationParams,
						scope: this,
						loadMask: true,
						success: function(response, options, decodedResponse) {
_debug('nodeExpandLevel2 localizations decodedResponse', decodedResponse);
//							var childAttributeNodeObject = {};
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = node;
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE;
//							childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
//
//							if (!Ext.Object.isEmpty(decodedResponse.response))
//								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
//									childAttributeNodeObject[tag] = translation;
//								});
//
//							node.appendChild(childAttributeNodeObject);
						}
					});
				}
			} else {
				_debug('ERROR: CMDBuild.controller.administration.localizations.advancedTranslationsTable.SectionClasses onAdvancedTableRowUpdateButtonClick node is empty');
			}
		}


	});

})();