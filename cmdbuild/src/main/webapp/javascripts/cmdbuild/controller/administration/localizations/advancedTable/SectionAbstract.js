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
		 * Build attributes main node
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 *
		 * @returns {CMDBuild.model.localizations.advancedTable.TreeStore}
		 */
		buildAttributesNode: function(rootNode) {
			if (!Ext.isEmpty(rootNode)) {
				var entityAttributesNodeObject = { expandable: true };
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.attributes;

				return rootNode.appendChild(entityAttributesNodeObject);
			}

			return rootNode;
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
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

						// Entity's attributes nodes
						if (!Ext.isEmpty(entityObject[CMDBuild.core.proxy.Constants.CHILDREN]))
							this.decodeStructureAttributes(entityMainNode, entityObject[CMDBuild.core.proxy.Constants.CHILDREN]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
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
				&& !Ext.isEmpty(attributesArray)
				&& Ext.isArray(attributesArray)
			) {
				rootNode = this.buildAttributesNode(rootNode);

				Ext.Array.forEach(attributesArray, function(attributeObject, i, allAttributesObjects) {
					if (!Ext.Array.contains(this.entityAttributeFilter, attributeObject[CMDBuild.core.proxy.Constants.NAME].toLowerCase())) { // Discard unwanted attributes
						var entityAttributeNodeObject = { expandable: true };
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = attributeObject[CMDBuild.core.proxy.Constants.NAME];
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
						entityAttributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = attributeObject[CMDBuild.core.proxy.Constants.NAME];

						var entityAttributeNode = rootNode.appendChild(entityAttributeNodeObject);

						this.decodeStructureFields(entityAttributeNode, attributeObject[CMDBuild.core.proxy.Constants.FIELDS]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureAttributes() - wrong parameters type', this);
			}
		},

		/**
		 * Entity translatable fields
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} fieldsArray
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
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();

					// Fields adapter for attributes nodes
					if (rootNode.getDepth() != 1) {
						entityFieldNodeObject[CMDBuild.core.proxy.Constants.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
						entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE + CMDBuild.core.Utils.toTitleCase(this.getSectionId());
					}

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
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
				_debug('[' + this.getSectionId() + '] fillWithTranslations() - wrong parameters type', this);
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
			params[CMDBuild.core.proxy.Constants.ACTIVE] = this.view.activeOnlyCheckbox.getValue();
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