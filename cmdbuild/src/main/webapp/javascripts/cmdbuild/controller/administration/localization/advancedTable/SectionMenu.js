(function() {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.SectionMenu', {
		extend: 'CMDBuild.controller.administration.localization.advancedTable.SectionAbstract',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.Constants.MENU,

		/**
		 * @property {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localization.advancedTable.SectionPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.advancedTable.SectionPanel', {
				delegate: this,
				hideActiveOnlyCheckbox: true,
				title: '@@ Menu'
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Build children main node (buildAttributesNode)
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 *
		 * @returns {CMDBuild.model.localization.advancedTable.TreeStore}
		 */
		buildChildrenNode: function(rootNode) {
			if (!Ext.isEmpty(rootNode) && rootNode.getDepth() != 1) {
				var entityAttributesNodeObject = { expandable: true };
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = '@@ Children';

				return rootNode.appendChild(entityAttributesNodeObject);
			}

			return rootNode;
		},

		/**
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} arrayToDecode
		 */
		decodeStructure: function(rootNode, arrayToDecode) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(arrayToDecode)
				&& Ext.isArray(arrayToDecode)
			) {
				Ext.Array.forEach(arrayToDecode, function(entityObject, i, allEntitiesObjects) {
					if (!Ext.Array.contains(this.entityFilter, entityObject[CMDBuild.core.proxy.Constants.NAME].toLowerCase())) { // Discard unwanted entities
						// Entity main node
						var entityMainNodeObject = { expandable: true };
						entityMainNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = entityObject[CMDBuild.core.proxy.Constants.NAME];
						entityMainNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						entityMainNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
						entityMainNodeObject[CMDBuild.core.proxy.Constants.TEXT] = entityObject[CMDBuild.core.proxy.Constants.NAME];

						var entityMainNode = rootNode.appendChild(entityMainNodeObject);

						// Entity's fields nodes
						if (!Ext.isEmpty(entityObject[CMDBuild.core.proxy.Constants.FIELDS]))
							this.decodeStructureFields(entityMainNode, entityObject[CMDBuild.core.proxy.Constants.FIELDS], entityObject);

						// Entity's children nodes
						if (!Ext.isEmpty(entityObject[CMDBuild.core.proxy.Constants.CHILDREN]))
							this.decodeStructureChildren(entityMainNode, entityObject[CMDBuild.core.proxy.Constants.CHILDREN]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		},

		/**
		 * Entity children nodes (decodeStructureAttributes)
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} attributesArray
		 */
		decodeStructureChildren: function(rootNode, attributesArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(attributesArray)
				&& Ext.isArray(attributesArray)
			) {
				rootNode = this.buildChildrenNode(rootNode);

				Ext.Array.forEach(attributesArray, function(attributeObject, i, allAttributesObjects) {
					if (!Ext.Array.contains(this.entityAttributeFilter, attributeObject[CMDBuild.core.proxy.Constants.NAME].toLowerCase())) { // Discard unwanted attributes
						var entityAttributeNodeObject = { expandable: true };
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = attributeObject[CMDBuild.core.proxy.Constants.NAME];
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = attributeObject.fields[0][CMDBuild.core.proxy.Constants.VALUE];
						entityAttributeNodeObject['iconCls'] = 'cmdbuild-tree-' + attributeObject[CMDBuild.core.proxy.Constants.TYPE] + '-icon';

						var entityAttributeNode = rootNode.appendChild(entityAttributeNodeObject);

						// Entity's fields nodes
						if (!Ext.isEmpty(attributeObject[CMDBuild.core.proxy.Constants.FIELDS]))
							this.decodeStructureFields(entityAttributeNode, attributeObject[CMDBuild.core.proxy.Constants.FIELDS]);

						// Entity's children nodes
						if (!Ext.isEmpty(attributeObject[CMDBuild.core.proxy.Constants.CHILDREN]))
							this.decodeStructureChildren(entityAttributeNode, attributeObject[CMDBuild.core.proxy.Constants.CHILDREN]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureChildren() - wrong parameters type', this);
			}
		},

		/**
		 * Menu translatable fields
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} fieldsArray
		 *
		 * @override
		 */
		decodeStructureFields: function(rootNode, fieldsArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(fieldsArray)
				&& Ext.isArray(fieldsArray)
			) {
				Ext.Array.forEach(fieldsArray, function(fieldObject, i, allFields) {
					var entityFieldNodeObject = {};
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = fieldObject[CMDBuild.core.proxy.Constants.VALUE];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.FIELD] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.getLevelNode(rootNode, 4).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.MENU_ITEM;

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		}
	});

})();