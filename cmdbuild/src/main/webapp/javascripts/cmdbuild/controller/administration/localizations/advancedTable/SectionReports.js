(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionReports', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionBase',

		requires: [
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.Constants',
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
		sectionId: CMDBuild.core.proxy.Constants.CLASS, // TODO

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionReportsPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.SectionReportsPanel', {
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
//					params[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();
//					params[CMDBuild.core.proxy.Constants.OWNER] = node.get(CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER);
//					params[CMDBuild.core.proxy.Constants.IDENTIFIER] = node.get(CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER);
//					params[CMDBuild.core.proxy.Constants.FIELD] = childNode.get(CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER);
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
//			params[CMDBuild.core.proxy.Constants.ACTIVE] = true;
//			params[CMDBuild.core.proxy.Constants.CLASS_NAME] = this.getLevelNode(node, 1).get(CMDBuild.core.proxy.Constants.TEXT);
//
//			CMDBuild.LoadMask.get().show();
//			CMDBuild.core.proxy.Attributes.read({
//				params: params,
//				scope: this,
//				success: function(response, options, decodedResponse) {
//					// Sort attributes with CMDBuild sort order
//					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.Constants.ATTRIBUTES], CMDBuild.core.proxy.Constants.INDEX);
//
//					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.Constants.ATTRIBUTES], function(childNode, i, allChildNodes) {
//						if (childNode[CMDBuild.core.proxy.Constants.NAME] != 'Notes') { // Custom CMDBuild behaviour
//							var localizationParams = {};
//							localizationParams[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE + this.getSectionId();
//							localizationParams[CMDBuild.core.proxy.Constants.OWNER] = this.getLevelNode(node, 1).get(CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER);
//							localizationParams[CMDBuild.core.proxy.Constants.IDENTIFIER] = childNode[CMDBuild.core.proxy.Constants.NAME];
//							localizationParams[CMDBuild.core.proxy.Constants.FIELD] = CMDBuild.core.proxy.Constants.DESCRIPTION;
//
//							CMDBuild.core.proxy.localizations.Localizations.read({
//								params: localizationParams,
//								scope: this,
//								loadMask: true,
//								success: function(response, options, decodedResponse) {
//									var childAttributeNodeObject = {};
//									childAttributeNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = childNode[CMDBuild.core.proxy.Constants.DESCRIPTION];
//									childAttributeNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.Constants.ATTRIBUTE + this.getSectionId();
//									childAttributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
//									childAttributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = node;
//									childAttributeNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = childNode[CMDBuild.core.proxy.Constants.NAME];
//									childAttributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = childNode[CMDBuild.core.proxy.Constants.NAME];
//
//									if (!Ext.Object.isEmpty(decodedResponse.response)) {
//										Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
//											childAttributeNodeObject[tag] = translation;
//										});
//
//										childAttributeNodeObject[CMDBuild.core.proxy.Constants.WAS_EMPTY] = false;
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
//			params[CMDBuild.core.proxy.Constants.ACTIVE] = true;
//
//			CMDBuild.core.proxy.Classes.read({
//				params: params,
//				loadMask: true,
//				scope: this,
//				success: function(response, options, decodedResponse) {
//					// Sort classes with CMDBuild sort order
//					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.Constants.CLASSES], CMDBuild.core.proxy.Constants.TEXT);
//
//					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.Constants.CLASSES], function(classObject, i, allClasses) {
//						if (
//							classObject[CMDBuild.core.proxy.Constants.TYPE] == 'class' // Discard processes from visualization
//							&& classObject[CMDBuild.core.proxy.Constants.NAME] != 'Class' // Discard root class of all classes
//						) {
//							// Class main node
//							var classMainNodeObject = { expandable: true, };
//							classMainNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = classObject[CMDBuild.core.proxy.Constants.NAME];
//							classMainNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
//							classMainNodeObject[CMDBuild.core.proxy.Constants.PARENT] = root;
//							classMainNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = classObject[CMDBuild.core.proxy.Constants.NAME];
//							classMainNodeObject[CMDBuild.core.proxy.Constants.TEXT] = classObject[CMDBuild.core.proxy.Constants.NAME];
//
//							var classMainNode = root.appendChild(classMainNodeObject);
//
//							// Class description property object
//							var classDescriptionNodeObject = {};
//							classDescriptionNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = classObject[CMDBuild.core.proxy.Constants.TEXT];
//							classDescriptionNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.Constants.TEXT;
//							classDescriptionNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
//							classDescriptionNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classMainNode;
//							classDescriptionNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.Constants.DESCRIPTION;
//							classDescriptionNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.descriptionLabel;
//
//							classMainNode.appendChild(classDescriptionNodeObject);
//
//							// Class attributes node (always displayed because Code and Description are default class attributes)
//							var classAttributeNodeObject = { expandable: true };
//							classAttributeNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.Constants.ATTRIBUTES;
//							classAttributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
//							classAttributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classMainNode;
//							classAttributeNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.Constants.ATTRIBUTES;
//							classAttributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.attributes;
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
//				var parentProperty = node.get(CMDBuild.core.proxy.Constants.PARENT).get(CMDBuild.core.proxy.Constants.PROPERTY);
//
//				var localizationParams = {};
//				localizationParams[CMDBuild.core.proxy.Constants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.Constants.NAME);
//				localizationParams[CMDBuild.core.proxy.Constants.CLASS_NAME] = this.getFirstLevelNode(node).get(CMDBuild.core.proxy.Constants.OBJECT);
//				localizationParams[CMDBuild.core.proxy.Constants.FIELD] = CMDBuild.core.proxy.Constants.DESCRIPTION;
//				localizationParams[CMDBuild.core.proxy.Constants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.Constants.OBJECT);
//				localizationParams[CMDBuild.core.proxy.Constants.TRANSLATIONS] = Ext.encode(node.getChanges());
//				localizationParams[CMDBuild.core.proxy.Constants.SECTION_ID] = (parentProperty == CMDBuild.core.proxy.Constants.CLASSES) ? this.getSectionId() : this.getSectionId() + CMDBuild.core.Utils.toTitleCase(parentProperty);
//
//				if (node.get(CMDBuild.core.proxy.Constants.WAS_EMPTY)) {
//					CMDBuild.core.proxy.localizations.Localizations.create({
//						params: localizationParams,
//						scope: this,
//						success: function(response, options, decodedResponse) {
//							node.set(CMDBuild.core.proxy.Constants.WAS_EMPTY, false);
//						}
//					});
//				} else {
//					CMDBuild.core.proxy.localizations.Localizations.update({
//						params: localizationParams,
//						scope: this,
//						success: function(response, options, decodedResponse) {
//							node.set(CMDBuild.core.proxy.Constants.WAS_EMPTY, false);
//						}
//					});
//				}
//			} else {
//				_error('empty node on update action', this);
//			}
		}
	});

})();