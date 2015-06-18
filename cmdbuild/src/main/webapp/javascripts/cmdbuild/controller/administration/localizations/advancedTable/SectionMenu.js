(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionMenu', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionBase',

		requires: [
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.localizations.Localizations',
//			'CMDBuild.model.localizations.advancedTable.TreeStore',
			'CMDBuild.core.Utils',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
//			'onAdvancedTableClassesShow',
//			'onAdvancedTableNodeExpand',
//			'onAdvancedTableRowUpdateButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.CMProxyConstants.CLASS, // TODO

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionMenuPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.SectionMenuPanel', {
				delegate: this
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Refresh all child node filling them with translations (class properties)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		nodeExpandLevel1: function(node) { // TODO implementare chiamate in blocco
//			node.eachChild(function(childNode) {
//				if (childNode.isLeaf()) {
//					var params = {};
//					params[CMDBuild.core.proxy.CMProxyConstants.TYPE] = this.getSectionId();
//					params[CMDBuild.core.proxy.CMProxyConstants.OWNER] = node.get(CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER);
//					params[CMDBuild.core.proxy.CMProxyConstants.IDENTIFIER] = node.get(CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER);
//					params[CMDBuild.core.proxy.CMProxyConstants.FIELD] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY_IDENTIFIER);
//
//					CMDBuild.core.proxy.localizations.Localizations.read({
//						params: params,
//						scope: this,
//						success: function(response, options, decodedResponse) {
//							// Fill node with translations
//							if (!Ext.Object.isEmpty(decodedResponse.response)) {
//								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
//									childNode.set(tag, translation);
//								});
//
//								childNode.commit();
//							}
//						}
//					});
//				}
//			}, this);
		},

		/**
		 * Rebuild all child node with translations (class attributes)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		nodeExpandLevel2: function(node) { // TODO implementare chiamate in blocco
//			node.removeAll();
//
//			var params = {};
//			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
//			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.getLevelNode(node, 1).get(CMDBuild.core.proxy.CMProxyConstants.TEXT);
//
//			CMDBuild.LoadMask.get().show();
//			CMDBuild.core.proxy.Attributes.read({
//				params: params,
//				scope: this,
//				success: function(response, options, decodedResponse) {
//					// Sort attributes with CMDBuild sort order
//					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], CMDBuild.core.proxy.CMProxyConstants.INDEX);
//
//					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], function(childNode, i, allChildNodes) {
//						if (childNode[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Notes') { // Custom CMDBuild behaviour
//							var localizationParams = {};
//							localizationParams[CMDBuild.core.proxy.CMProxyConstants.TYPE] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE + this.getSectionId();
//							localizationParams[CMDBuild.core.proxy.CMProxyConstants.OWNER] = this.getLevelNode(node, 1).get(CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER);
//							localizationParams[CMDBuild.core.proxy.CMProxyConstants.IDENTIFIER] = childNode[CMDBuild.core.proxy.CMProxyConstants.NAME];
//							localizationParams[CMDBuild.core.proxy.CMProxyConstants.FIELD] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
//
//							CMDBuild.core.proxy.localizations.Localizations.read({
//								params: localizationParams,
//								scope: this,
//								loadMask: true,
//								success: function(response, options, decodedResponse) {
//									var childAttributeNodeObject = {};
//									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = childNode[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
//									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE + this.getSectionId();
//									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
//									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = node;
//									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY_IDENTIFIER] = childNode[CMDBuild.core.proxy.CMProxyConstants.NAME];
//									childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.TEXT] = childNode[CMDBuild.core.proxy.CMProxyConstants.NAME];
//
//									if (!Ext.Object.isEmpty(decodedResponse.response)) {
//										Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
//											childAttributeNodeObject[tag] = translation;
//										});
//
//										childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY] = false;
//									}
//
//									node.appendChild(childAttributeNodeObject);
//								}
//							});
//						}
//					}, this);
//				},
//				callback: function(records, operation, success) {
//					CMDBuild.LoadMask.get().hide();
//				}
//			});
		},

		/**
		 * Fill grid store with classes data
		 */
		onAdvancedTableClassesShow: function() { // TODO implementare chiamate in blocco
//			var root = this.grid.getStore().getRootNode();
//			root.removeAll();
//
//			// GetAllClasses data to get default translations
//			var params = {};
//			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
//
//			CMDBuild.core.proxy.Classes.read({
//				params: params,
//				loadMask: true,
//				scope: this,
//				success: function(response, options, decodedResponse) {
//					// Sort classes with CMDBuild sort order
//					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CLASSES], CMDBuild.core.proxy.CMProxyConstants.TEXT);
//
//					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CLASSES], function(classObject, i, allClasses) {
//						if (
//							classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
//							&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
//						) {
//							// Class main node
//							var classMainNodeObject = { expandable: true, };
//							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
//							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
//							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = root;
//							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY_IDENTIFIER] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
//							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.TEXT] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
//
//							var classMainNode = root.appendChild(classMainNodeObject);
//
//							// Class description property object
//							var classDescriptionNodeObject = {};
//							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
//							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.CMProxyConstants.TEXT;
//							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
//							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classMainNode;
//							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
//							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.TEXT] = CMDBuild.Translation.descriptionLabel;
//
//							classMainNode.appendChild(classDescriptionNodeObject);
//
//							// Class attributes node (always displayed because Code and Description are default class attributes)
//							var classAttributeNodeObject = { expandable: true };
//							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES;
//							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
//							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classMainNode;
//							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES;
//							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.TEXT] = CMDBuild.Translation.attributes;
//
//							var classAttributesNode = classMainNode.appendChild(classAttributeNodeObject);
//
//							classAttributesNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make attributes node expandable
//						}
//					}, this);
//				}
//			});
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		onAdvancedTableRowUpdateButtonClick: function(node) {  // TODO implementare nuove chiamate
//			if (!Ext.Object.isEmpty(node)) {
//				var parentProperty = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);
//
//				var localizationParams = {};
//				localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
//				localizationParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.getFirstLevelNode(node).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
//				localizationParams[CMDBuild.core.proxy.CMProxyConstants.FIELD] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
//				localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
//				localizationParams[CMDBuild.core.proxy.CMProxyConstants.TRANSLATIONS] = Ext.encode(node.getChanges());
//				localizationParams[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = (parentProperty == CMDBuild.core.proxy.CMProxyConstants.CLASSES) ? this.getSectionId() : this.getSectionId() + CMDBuild.core.Utils.toTitleCase(parentProperty);
//
//				if (node.get(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY)) {
//					CMDBuild.core.proxy.localizations.Localizations.create({
//						params: localizationParams,
//						scope: this,
//						success: function(response, options, decodedResponse) {
//							node.set(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, false);
//						}
//					});
//				} else {
//					CMDBuild.core.proxy.localizations.Localizations.update({
//						params: localizationParams,
//						scope: this,
//						success: function(response, options, decodedResponse) {
//							node.set(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, false);
//						}
//					});
//				}
//			} else {
//				_error('empty node on update action', this);
//			}
		}
	});

})();