(function() {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.SectionProcess', {
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
		 * @cfg {Array}
		 */
		entityAttributeFilter: ['notes'],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.Constants.PROCESS,

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
				title: '@@ Processes'
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Process translatable fields
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
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
					entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.CLASS;

					// Fields adapter for attributes nodes
					if (rootNode.getDepth() != 1) {
						entityFieldNodeObject[CMDBuild.core.proxy.Constants.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.proxy.Constants.IDENTIFIER);
						entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE + CMDBuild.core.Utils.toTitleCase(CMDBuild.core.proxy.Constants.CLASS);
					}

					this.fillWithTranslations(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		},
	});

})();