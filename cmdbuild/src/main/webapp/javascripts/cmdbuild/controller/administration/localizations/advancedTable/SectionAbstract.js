(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionAbstract', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localizations.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAdvancedTableRowUpdateButtonClick',
			'onAdvancedTableShow = onAdvancedTableOnlyEnabledEntitiesCheck'
		],

		/**
		 * @cfg {Array}
		 */
		entityFilter: [],

		/**
		 * @cfg {Array}
		 */
		entityAttributeFilter: [],

		/**
		 * @cfg {String}
		 *
		 * @abstract
		 */
		sectionId: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionPanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} arrayToDecode
		 */
		decodeStructure: function(rootNode, arrayToDecode) {
			if (
				Ext.isArray(arrayToDecode)
				&& !Ext.isEmpty(rootNode)
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

						this.decodeStructureFields(entityMainNode, entityObject[CMDBuild.core.proxy.Constants.FIELDS], entityObject);

						// Entity attribute nodes
						if (Ext.isArray(entityObject[CMDBuild.core.proxy.Constants.ATTRIBUTES])) {
							var entityAttributesNodeObject = { expandable: true };
							entityAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
							entityAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = entityMainNode;
							entityAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.attributes;

							var entityAttributesNode = entityMainNode.appendChild(entityAttributesNodeObject);

							this.decodeStructureAttributes(entityAttributesNode, entityObject[CMDBuild.core.proxy.Constants.ATTRIBUTES]);
						}
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		},

		/**
		 * Entity attribute node
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} fieldsArray
		 */
		decodeStructureAttributeFields: function(rootNode, fieldsArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& Ext.isArray(fieldsArray)
			) {
				Ext.Array.forEach(fieldsArray, function(fieldObject, i, allAttributesObjects) {
					var attributeFieldNodeObject = {};
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = fieldObject[CMDBuild.core.proxy.Constants.VALUE];
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.FIELD] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.getLevelNode(rootNode, 3).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE + CMDBuild.core.Utils.toTitleCase(this.getSectionId());

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], attributeFieldNodeObject);

					rootNode.appendChild(attributeFieldNodeObject);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureAttributeFields() - wrong params type', this);
			}
		},

		/**
		 * Entity attribute nodes
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} attributesArray
		 */
		decodeStructureAttributes: function(rootNode, attributesArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& Ext.isArray(attributesArray)
			) {
				Ext.Array.forEach(attributesArray, function(attributeObject, i, allAttributesObjects) {
					if (!Ext.Array.contains(this.entityAttributeFilter, attributeObject[CMDBuild.core.proxy.Constants.NAME].toLowerCase())) { // Discard unwanted attributes
						var entityAttributeNodeObject = { expandable: true };
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = attributeObject[CMDBuild.core.proxy.Constants.NAME];
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = attributeObject[CMDBuild.core.proxy.Constants.NAME];

						var entityAttributeNode = rootNode.appendChild(entityAttributeNodeObject);

						this.decodeStructureAttributeFields(entityAttributeNode, attributeObject[CMDBuild.core.proxy.Constants.FIELDS]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureAttributes() - wrong params type', this);
			}
		},

		/**
		 * Entity translatable fields
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} fieldsArray
		 */
		decodeStructureFields: function(rootNode, fieldsArray) {
			if (Ext.isArray(fieldsArray)) {
				Ext.Array.forEach(fieldsArray, function(fieldObject, i, allFields) {
					var entityFieldNodeObject = {};
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = fieldObject[CMDBuild.core.proxy.Constants.VALUE];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.FIELD] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make attributes node expandable
			}
		},

		/**
		 * @param {Object} translationsSourceObject
		 * @param {Object} targetObject
		 */
		fillWithTranslations: function(translationsSourceObject, targetObject) {
			if (
				Ext.isObject(translationsSourceObject)
				&& Ext.isObject(targetObject)
			) {
				Ext.Object.each(translationsSourceObject, function(tag, translation, myself) {
					targetObject[tag] = translation;
				});
			} else {
				_debug('[' + this.getSectionId() + '] fillWithTranslations() - wrong params type', this);
			}
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} startNode
		 * @param {Number} levelToReach
		 *
		 * @returns {CMDBuild.model.localizations.advancedTable.TreeStore} requestedNode or null
		 */
		getLevelNode: function(startNode, levelToReach) {
			var requestedNode = startNode;
			if (!Ext.isEmpty(requestedNode) && Ext.isNumber(levelToReach)) {
				while (requestedNode.getDepth() > levelToReach) {
					requestedNode = requestedNode.get(CMDBuild.core.proxy.Constants.PARENT);
				}

				return requestedNode;
			}

			return null;
		},

		/**
		 * @returns {String}
		 */
		getSectionId: function() {
			return this.sectionId;
		},

		/**
		 * Fill grid store with entities data
		 */
		onAdvancedTableShow: function() {
			var root = this.grid.getStore().getRootNode();
			root.removeAll();

			var params = {};
			params[CMDBuild.core.proxy.Constants.ACTIVE] = this.view.includeOnlyEnabledEntitiesCheckbox.getValue();
			params[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();

			CMDBuild.core.proxy.localizations.Localizations.readStructure({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					Ext.suspendLayouts();

					this.decodeStructure(root, decodedResponse.response);

					Ext.resumeLayouts(true);
				}
			});
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		onAdvancedTableRowUpdateButtonClick: function(node) {
			if (!Ext.isEmpty(node)) {
				var params = {};
				params[CMDBuild.core.proxy.Constants.TYPE] = node.get(CMDBuild.core.proxy.Constants.TYPE);
				params[CMDBuild.core.proxy.Constants.IDENTIFIER] = node.get(CMDBuild.core.proxy.Constants.IDENTIFIER);
				params[CMDBuild.core.proxy.Constants.FIELD] = node.get(CMDBuild.core.proxy.Constants.FIELD);
				params[CMDBuild.core.proxy.Constants.TRANSLATIONS] = Ext.encode(node.getTranslations());

				if (!Ext.isEmpty(node.get(CMDBuild.core.proxy.Constants.OWNER)))
					params[CMDBuild.core.proxy.Constants.OWNER] = node.get(CMDBuild.core.proxy.Constants.OWNER);

				CMDBuild.core.proxy.localizations.Localizations.update({
					params: params,
					success: function(response, options, decodedResponse) {
						CMDBuild.core.Message.success();
					}
				});
			} else {
				_error('empty node on update action', this);
			}
		}
	});

})();