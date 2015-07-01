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

					// Lookup values nodes
					if (Ext.isArray(lookupTypeObject[CMDBuild.core.proxy.Constants.VALUES])) {
						var lookupValuesNodeObject = { expandable: true };
						lookupValuesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						lookupValuesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = lookupTypeNode;
						lookupValuesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = '@@ Lookup'; // TODO: translation???

						var lookupValuesNode = lookupTypeNode.appendChild(lookupValuesNodeObject);

						this.decodeStructureValues(lookupValuesNode, lookupTypeObject[CMDBuild.core.proxy.Constants.VALUES]);
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
				&& !Ext.isEmpty(fieldsArray)
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
					attributeFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId() + CMDBuild.core.Utils.toTitleCase(CMDBuild.core.proxy.Constants.VALUE);

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], attributeFieldNodeObject);

					rootNode.appendChild(attributeFieldNodeObject);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureAttributeFields() - wrong parameters type', this);
			}
		},

		/**
		 * Entity attribute nodes
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
				Ext.Array.forEach(valuesArray, function(valueObject, i, allValuesObjects) {
					var lookupValueNodeObject = { expandable: true };
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = valueObject[CMDBuild.core.proxy.Constants.TRANSLATION_UUID];
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					lookupValueNodeObject[CMDBuild.core.proxy.Constants.TEXT] = valueObject[CMDBuild.core.proxy.Constants.CODE];

					var lookupValueNode = rootNode.appendChild(lookupValueNodeObject);

					this.decodeStructureAttributeFields(lookupValueNode, valueObject[CMDBuild.core.proxy.Constants.FIELDS]);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureAttributes() - wrong parameters type', this);
			}
		}
	});

})();