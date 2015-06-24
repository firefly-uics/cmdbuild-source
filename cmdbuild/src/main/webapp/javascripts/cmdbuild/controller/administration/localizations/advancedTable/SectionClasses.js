(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionClasses', {
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
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAdvancedTableShow',
			'onAdvancedTableRowUpdateButtonClick'
		],

		/**
		 * @cfg {Array}
		 */
		entityAttributeFilter: ['Notes'],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.Constants.CLASS,

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
				title: '@@ Classes'
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
				Ext.isArray(arrayToDecode)
				&& !Ext.isEmpty(rootNode)
			) {
				// TODO: class order
				Ext.Array.forEach(arrayToDecode, function(entityObject, i, allEntitiesObjects) {
					if (entityObject[CMDBuild.core.proxy.Constants.NAME].toLowerCase() != 'class') { // Discard root class of all classes
						// Entity main node
						var entityMainNodeObject = { expandable: true };
						entityMainNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						entityMainNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
						entityMainNodeObject[CMDBuild.core.proxy.Constants.TEXT] = entityObject[CMDBuild.core.proxy.Constants.NAME];

						var classMainNode = rootNode.appendChild(entityMainNodeObject);

						this.decodeStructureFields(classMainNode, entityObject[CMDBuild.core.proxy.Constants.FIELDS], entityObject);

						// Entity attribute nodes
						if (Ext.isArray(entityObject[CMDBuild.core.proxy.Constants.ATTRIBUTES])) {
							var entityAttributesNodeObject = { expandable: true };
							entityAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
							entityAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classMainNode;
							entityAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.attributes;

							var classAttributesNode = classMainNode.appendChild(entityAttributesNodeObject);

							this.decodeStructureAttributes(classAttributesNode, entityObject[CMDBuild.core.proxy.Constants.ATTRIBUTES], entityObject);
						}
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		}
	});

})();