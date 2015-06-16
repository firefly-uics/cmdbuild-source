(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionClasses', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionBase',

		requires: [
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.localizations.Localizations',
			'CMDBuild.model.localizations.advancedTable.TreeStore',
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
			'onAdvancedTableClassesShow',
			'onAdvancedTableNodeExpand',
			'onAdvancedTableRowUpdateButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.CMProxyConstants.CLASSES,

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionClassesPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.SectionClassesPanel', {
				delegate: this
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Refresh all child node filling them with translations (class description)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		nodeExpandLevel1: function(node) {
			node.eachChild(function(childNode) {
				if (childNode.isLeaf()) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
					params[CMDBuild.core.proxy.CMProxyConstants.FIELD] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);
					params[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = this.getSectionId();

					CMDBuild.core.proxy.localizations.Localizations.read({
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

							if (!Ext.Object.isEmpty(decodedResponse.response))
								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
									classPropertyNodeObject[tag] = translation;
								});

							node.replaceChild(classPropertyNodeObject, childNode);
						}
					});
				}
			}, this);
		},

		/**
		 * Rebuild all child node with translations (class attributes)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		nodeExpandLevel2: function(node) {
			node.removeAll();

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.Attributes.read({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					// Sort attributes with CMDBuild sort order
					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], CMDBuild.core.proxy.CMProxyConstants.INDEX);

					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], function(attributeObject, i, allAttributes) {
						if (attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Notes') { // Custom CMDBuild behaviour
							var localizationParams = {};
							localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
							localizationParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
							localizationParams[CMDBuild.core.proxy.CMProxyConstants.FIELD] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
							localizationParams[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = this.getSectionId() + CMDBuild.core.Utils.toTitleCase(node.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY));

							CMDBuild.core.proxy.localizations.Localizations.read({
								params: localizationParams,
								scope: this,
								loadMask: true,
								success: function(response, options, decodedResponse) {
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

										childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY] = false;
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
		 * Fill grid store with classes data
		 */
		onAdvancedTableClassesShow: function() {
			var root = this.grid.getStore().getRootNode();
			root.removeAll();

			// GetAllClasses data to get default translations
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;

			CMDBuild.core.proxy.Classes.read({
				params: params,
				loadMask: true,
				scope: this,
				success: function(response, options, decodedResponse) {
					// Sort classes with CMDBuild sort order
					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CLASSES], CMDBuild.core.proxy.CMProxyConstants.TEXT);

					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CLASSES], function(classObject, i, allClasses) {
						if (
							classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
							&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
						) {
							// Class main node
							var classMainNodeObject = { expandable: true, };
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = root;
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.CLASSES;

							var classMainNode = root.appendChild(classMainNodeObject);

							// Class description property object
							var classDescriptionNodeObject = {};
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Description';
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classMainNode;
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;

							classMainNode.appendChild(classDescriptionNodeObject);

							// Class attributes node (always displayed because Code and Description are default class attributes)
							var classAttributeNodeObject = { expandable: true };
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Attributes';
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = classMainNode;
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES;

							var classAttributesNode = classMainNode.appendChild(classAttributeNodeObject);

							classAttributesNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make attributes node expandable
						}
					}, this);
				}
			});
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		onAdvancedTableRowUpdateButtonClick: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				var parentProperty = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);

				var localizationParams = {};
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.getFirstLevelNode(node).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.FIELD] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.TRANSLATIONS] = Ext.encode(node.getChanges());
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = (parentProperty == CMDBuild.core.proxy.CMProxyConstants.CLASSES) ? this.getSectionId() : this.getSectionId() + CMDBuild.core.Utils.toTitleCase(parentProperty);

				if (node.get(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY)) {
					CMDBuild.core.proxy.localizations.Localizations.create({
						params: localizationParams,
						scope: this,
						success: function(response, options, decodedResponse) {
							node.set(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, false);
						}
					});
				} else {
					CMDBuild.core.proxy.localizations.Localizations.update({
						params: localizationParams,
						scope: this,
						success: function(response, options, decodedResponse) {
							node.set(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, false);
						}
					});
				}
			} else {
				_error('empty node on update action', this);
			}
		}
	});

})();