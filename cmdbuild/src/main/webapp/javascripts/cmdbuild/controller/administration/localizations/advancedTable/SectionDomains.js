(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionDomains', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionBase',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Domain',
			'CMDBuild.core.proxy.localizations.Localizations',
			'CMDBuild.model.localizations.advancedTable.TreeStore'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAdvancedTableDomainShow',
			'onAdvancedTableNodeExpand',
			'onAdvancedTableRowUpdateButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.Constants.DOMAIN,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionDomainsPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.SectionDomainsPanel', {
				delegate: this
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Refresh all child node filling them with translations (domain descriptions)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		nodeExpandLevel1: function(node) { // TODO implementare chiamate in blocco
			node.eachChild(function(childNode) {
				if (childNode.isLeaf()) {
					var params = {};
					params[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();
					params[CMDBuild.core.proxy.Constants.OWNER] = node.get(CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER);
					params[CMDBuild.core.proxy.Constants.IDENTIFIER] = node.get(CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER);
					params[CMDBuild.core.proxy.Constants.FIELD] = childNode.get(CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER);

					CMDBuild.core.proxy.localizations.Localizations.read({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							// Fill node with translations
							if (!Ext.Object.isEmpty(decodedResponse.response)) {
								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
									childNode.set(tag, translation);
								});

								childNode.commit();
							}
						}
					});
				}
			}, this);
		},

		/**
		 * Fills all child node with translations (domain attributes)
		 *
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		nodeExpandLevel2: function(node) { // TODO implementare chiamate in blocco
			Ext.Array.forEach(node.childNodes, function(childNode, i, allChildNodes) {
				if (childNode[CMDBuild.core.proxy.Constants.NAME] != 'Notes') { // Custom CMDBuild behaviour
					var localizationParams = {};
					localizationParams[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE + this.getSectionId();
					localizationParams[CMDBuild.core.proxy.Constants.OWNER] = this.getLevelNode(node, 1).get(CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER);
					localizationParams[CMDBuild.core.proxy.Constants.IDENTIFIER] = childNode.get(CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER);
					localizationParams[CMDBuild.core.proxy.Constants.FIELD] = CMDBuild.core.proxy.Constants.DESCRIPTION;

					CMDBuild.core.proxy.localizations.Localizations.read({
						params: localizationParams,
						scope: this,
						loadMask: true,
						success: function(response, options, decodedResponse) {
							// Fill node with translations
							if (!Ext.Object.isEmpty(decodedResponse.response)) {
								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
									node.childNodes[i].set(tag, translation);
								});

								node.childNodes[i].set(CMDBuild.core.proxy.Constants.WAS_EMPTY, false);
							}

							node.childNodes[i].commit();
						}
					});
				}
			}, this);
		},

		/**
		 * Fill grid store with domains data
		 */
		onAdvancedTableDomainShow: function() { // TODO implementare chiamate in blocco
			var root = this.grid.getStore().getRootNode();
			root.removeAll();

			// GetAllDomains data to get default translations
			CMDBuild.core.proxy.Domain.getAll({
				loadMask: true,
				scope: this,
				success: function(response, options, decodedResponse) {
					// Sort attributes with CMDBuild sort order
					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.Constants.DOMAINS], CMDBuild.core.proxy.Constants.DESCRIPTION);

					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.Constants.DOMAINS], function(domainObject, i, allDomains) {
						// Sort attributes with CMDBuild sort order
						CMDBuild.core.Utils.objectArraySort(domainObject[CMDBuild.core.proxy.Constants.ATTRIBUTES], CMDBuild.core.proxy.Constants.INDEX);

						// Domain main node
						var domainMainNodeObject = { expandable: true, };
						domainMainNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = domainObject[CMDBuild.core.proxy.Constants.NAME];
						domainMainNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
						domainMainNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
						domainMainNodeObject[CMDBuild.core.proxy.Constants.PARENT] = root;
						domainMainNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
						domainMainNodeObject[CMDBuild.core.proxy.Constants.TEXT] = domainObject[CMDBuild.core.proxy.Constants.NAME];

						var domainMainNode = root.appendChild(domainMainNodeObject);

						// Domain description property object
						var domainDescriptionNodeObject = {};
						domainDescriptionNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = domainObject[CMDBuild.core.proxy.Constants.DESCRIPTION];
						domainDescriptionNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
						domainDescriptionNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
						domainDescriptionNodeObject[CMDBuild.core.proxy.Constants.PARENT] = domainMainNode;
						domainDescriptionNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.Constants.DESCRIPTION;
						domainDescriptionNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.descriptionLabel;

						domainMainNode.appendChild(domainDescriptionNodeObject);

						// Domain direct description property object
						var domainDirectDescriptionNodeObject = {};
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = domainObject['descrdir'];
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.Constants.PARENT] = domainMainNode;
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.Constants.DIRECT_DESCRIPTION;
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.directDescription;

						domainMainNode.appendChild(domainDirectDescriptionNodeObject);

						// Domain inverse description property object
						var domainInverseDescriptionNodeObject = {};
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = domainObject['descrinv'];
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.Constants.PARENT] = domainMainNode;
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.Constants.INVERSE_DESCRIPTION;
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.inverseDescription;

						domainMainNode.appendChild(domainInverseDescriptionNodeObject);

						// Domain master detail label property object
						if (Ext.isBoolean(domainObject['md']) && domainObject['md']) {
							var domainMasterDetailLabelNodeObject = {};
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = domainObject['md_label'];
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.Constants.PARENT] = domainMainNode;
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = 'md_label';
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.masterDetailLabel;

							domainMainNode.appendChild(domainMasterDetailLabelNodeObject);
						}

						// Domain attributes node
						if (
							Ext.isArray(domainObject[CMDBuild.core.proxy.Constants.ATTRIBUTES])
							&& !Ext.isEmpty(domainObject[CMDBuild.core.proxy.Constants.ATTRIBUTES])
						) {
							var domainAttributesNodeObject = { expandable: true };
							domainAttributesNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = domainObject[CMDBuild.core.proxy.Constants.NAME];
							domainAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
							domainAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = domainMainNode;
							domainAttributesNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = CMDBuild.core.proxy.Constants.ATTRIBUTES;
							domainAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.attributes;

							var domainAttributesNode = domainMainNode.appendChild(domainAttributesNodeObject);

							// Domain attributes child node
							Ext.Array.forEach(domainObject[CMDBuild.core.proxy.Constants.ATTRIBUTES], function(attributeObject, i, allAttributes) {
								var domainAttributeNodeObject = {};
								domainAttributeNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = attributeObject[CMDBuild.core.proxy.Constants.DESCRIPTION];
								domainAttributeNodeObject[CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER] = CMDBuild.core.proxy.Constants.ATTRIBUTE + this.getSectionId();
								domainAttributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
								domainAttributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = domainAttributesNode;
								domainAttributeNodeObject[CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER] = attributeObject[CMDBuild.core.proxy.Constants.NAME];
								domainAttributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = attributeObject[CMDBuild.core.proxy.Constants.NAME];

								domainAttributesNode.appendChild(domainAttributeNodeObject);
							}, this);
						}
					}, this);
				}
			});
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		onAdvancedTableRowUpdateButtonClick: function(node) {
//			if (!Ext.Object.isEmpty(node)) {
//				var parentProperty = node.get(CMDBuild.core.proxy.Constants.PARENT).get(CMDBuild.core.proxy.Constants.PROPERTY);
//
//				var localizationParams = {};
//				localizationParams[CMDBuild.core.proxy.Constants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.Constants.NAME);
//				localizationParams[CMDBuild.core.proxy.Constants.DOMAIN_NAME] = this.getFirstLevelNode(node).get(CMDBuild.core.proxy.Constants.OBJECT);
//				localizationParams[CMDBuild.core.proxy.Constants.FIELD] = CMDBuild.core.proxy.Constants.DESCRIPTION;
//				localizationParams[CMDBuild.core.proxy.Constants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.Constants.OBJECT);
//				localizationParams[CMDBuild.core.proxy.Constants.TRANSLATIONS] = Ext.encode(node.getChanges());
//				localizationParams[CMDBuild.core.proxy.Constants.SECTION_ID] = (parentProperty == CMDBuild.core.proxy.Constants.DOMAINS) ? this.getSectionId() : this.getSectionId() + CMDBuild.core.Utils.toTitleCase(parentProperty);
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