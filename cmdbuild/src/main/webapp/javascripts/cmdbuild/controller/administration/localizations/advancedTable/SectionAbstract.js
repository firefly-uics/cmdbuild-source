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
		entityAttributeFilter: [],

		/**
		 * @cfg {String}
		 *
		 * @abstract
		 */
		sectionId: undefined,

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} arrayToDecode
		 */
		decodeStructure: function(rootNode, arrayToDecode) {
			if (
				Ext.isArray(arrayToDecode)
				&& !Ext.isEmpty(rootNode)
			) {
				// TODO: class order
				Ext.Array.forEach(arrayToDecode, function(entityObject, i, allEntitiesObjects) {
					// Entity main node
					var entityMainNodeObject = { expandable: true };
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

						this.decodeStructureAttributes(entityAttributesNode, entityObject[CMDBuild.core.proxy.Constants.ATTRIBUTES], entityObject);
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
		 * @param {Object} ownerEntityObject
		 */
		decodeStructureAttributes: function(rootNode, attributesArray, ownerEntityObject) {
			// TODO: sort attributes with CMDBuild sort order
			if (
				!Ext.isEmpty(rootNode)
				&& Ext.isArray(attributesArray)
				&& Ext.isObject(ownerEntityObject)
			) {
				Ext.Array.forEach(attributesArray, function(attribute, i, allAttributes) {
					if (!Ext.Array.contains(this.entityAttributeFilter, attribute[CMDBuild.core.proxy.Constants.NAME])) { // Custom CMDBuild behaviour
						var attributeDescription = attribute[CMDBuild.core.proxy.Constants.FIELDS][0]; // In future could be more than only description to translate

						var attributeNodeObject = {};
						attributeNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = attributeDescription[CMDBuild.core.proxy.Constants.VALUE];
						attributeNodeObject[CMDBuild.core.proxy.Constants.FIELD] = attributeDescription[CMDBuild.core.proxy.Constants.NAME];
						attributeNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = attribute[CMDBuild.core.proxy.Constants.NAME];
						attributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
						attributeNodeObject[CMDBuild.core.proxy.Constants.OWNER] = ownerEntityObject[CMDBuild.core.proxy.Constants.NAME];
						attributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
						attributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = attribute[CMDBuild.core.proxy.Constants.NAME];
						attributeNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE + CMDBuild.core.Utils.toTitleCase(this.getSectionId());

						this.fillWithTranslations(attributeDescription[CMDBuild.core.proxy.Constants.TRANSLATIONS], attributeNodeObject);

						rootNode.appendChild(attributeNodeObject);
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
		 * @param {Object} ownerEntityObject
		 */
		decodeStructureFields: function(rootNode, fieldsArray, ownerEntityObject) {
			if (Ext.isArray(fieldsArray)) {
				Ext.Array.forEach(fieldsArray, function(fieldObject, i, allFields) {
					var entityFieldNodeObject = {};
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = fieldObject[CMDBuild.core.proxy.Constants.VALUE];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.FIELD] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = ownerEntityObject[CMDBuild.core.proxy.Constants.NAME];
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
			params[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();

			CMDBuild.core.proxy.localizations.Localizations.readStructure({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					Ext.suspendLayouts();

					this.decodeStructure(root, decodedResponse.response);

					Ext.resumeLayouts();
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