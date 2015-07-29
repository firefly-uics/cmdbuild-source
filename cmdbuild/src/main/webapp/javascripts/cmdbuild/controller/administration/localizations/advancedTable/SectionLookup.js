(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionLookup', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionAbstract',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localizations.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.Constants.LOOKUP,

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.SectionPanel', {
				delegate: this,
				title: '@@ Lookup types'
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Build value main node (buildAttributesNode)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 *
		 * @returns {CMDBuild.model.localizations.advancedTable.TreeStore}
		 *
		 * @override
		 */
		buildValuesNode: function(rootNode) {
			if (!Ext.isEmpty(rootNode)) {
				var entityAttributesNodeObject = { expandable: true };
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
				entityAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = '@@ Lookup'; // TODO: translation???

				return rootNode.appendChild(entityAttributesNodeObject);
			}

			return rootNode;
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} arrayToDecode
		 *
		 * @override
		 */
		decodeStructure: function(rootNode, arrayToDecode) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(arrayToDecode)
				&& Ext.isArray(arrayToDecode)
			) {
				Ext.Array.forEach(arrayToDecode, function(lookupTypeObject, i, allLookupTypesObjects) {
					// LookupType main node
					var lookupTypeNodeObject = { expandable: true };
					lookupTypeNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = lookupTypeObject[CMDBuild.core.proxy.Constants.DESCRIPTION];
					lookupTypeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
					lookupTypeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					lookupTypeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = lookupTypeObject[CMDBuild.core.proxy.Constants.DESCRIPTION];

					var lookupTypeNode = rootNode.appendChild(lookupTypeNodeObject);

					// Lookup's values nodes
					if (!Ext.isEmpty(lookupTypeObject[CMDBuild.core.proxy.Constants.VALUES]))
						this.decodeStructureValues(lookupTypeNode, lookupTypeObject[CMDBuild.core.proxy.Constants.VALUES]);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		},

		/**
		 * Entity translatable fields
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
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
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.getLevelNode(rootNode, 3).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();

					// Fields adapter for attributes nodes
					if (rootNode.getDepth() != 1) {
						entityFieldNodeObject[CMDBuild.core.proxy.Constants.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
						entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId() + CMDBuild.core.Utils.toTitleCase(CMDBuild.core.proxy.Constants.VALUE);
					}

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		},

		/**
		 * Entity values nodes (decodeStructureAttributes)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} rootNode
		 * @param {Array} valuesArray
		 */
		decodeStructureValues: function(rootNode, valuesArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(valuesArray)
				&& Ext.isArray(valuesArray)
			) {
				rootNode = this.buildValuesNode(rootNode);

				Ext.Array.forEach(valuesArray, function(valueObject, i, allValuesObjects) {
					var lookupValueNodeObject = { expandable: true };
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = valueObject[CMDBuild.core.proxy.Constants.TRANSLATION_UUID];
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.TEXT] = valueObject[CMDBuild.core.proxy.Constants.CODE];

					var lookupValueNode = rootNode.appendChild(lookupValueNodeObject);

					this.decodeStructureFields(lookupValueNode, valueObject[CMDBuild.core.proxy.Constants.FIELDS]);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureValues() - wrong parameters type', this);
			}
		}
	});

})();