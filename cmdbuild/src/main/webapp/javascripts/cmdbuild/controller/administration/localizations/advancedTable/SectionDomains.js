(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionDomains', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionBase',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
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
		sectionId: CMDBuild.core.proxy.CMProxyConstants.DOMAINS,

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
		nodeExpandLevel1: function(node) {
			node.eachChild(function(childNode) {
				if (childNode.isLeaf()) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
					params[CMDBuild.core.proxy.CMProxyConstants.FIELD] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);
					params[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = this.getSectionId();

					CMDBuild.core.proxy.localizations.Localizations.read({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							// Domain property object
							var domainPropertyNodeObject = {};
							domainPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.DEFAULT);
							domainPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							domainPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
							domainPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = node;
							domainPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);

							if (!Ext.Object.isEmpty(decodedResponse.response))
								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
									domainPropertyNodeObject[tag] = translation;
								});

							node.replaceChild(domainPropertyNodeObject, childNode);
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
		nodeExpandLevel2: function(node) {
			Ext.Array.forEach(node.childNodes, function(attributeObject, i, allAttributes) {
				if (attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Notes') { // Custom CMDBuild behaviour
					var localizationParams = {};
					localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = attributeObject.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
					localizationParams[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME] = this.getFirstLevelNode(node).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
					localizationParams[CMDBuild.core.proxy.CMProxyConstants.FIELD] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
					localizationParams[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = this.getSectionId() + CMDBuild.core.Utils.toTitleCase(node.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY));

					CMDBuild.core.proxy.localizations.Localizations.read({
						params: localizationParams,
						scope: this,
						loadMask: true,
						success: function(response, options, decodedResponse) {
							if (!Ext.Object.isEmpty(decodedResponse.response)) {
								Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
									node.childNodes[i].set(tag, translation);
								});

								node.childNodes[i].set(CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, false);
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
		onAdvancedTableDomainShow: function() {
			var root = this.grid.getStore().getRootNode();
			root.removeAll();

			// GetAllDomains data to get default translations
			CMDBuild.core.proxy.Domain.getAll({
				loadMask: true,
				scope: this,
				success: function(response, options, decodedResponse) {
					// Sort attributes with CMDBuild sort order
					CMDBuild.core.Utils.objectArraySort(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.DOMAINS], CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION);

					Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.DOMAINS], function(domainObject, i, allDomains) {
						// Sort attributes with CMDBuild sort order
						CMDBuild.core.Utils.objectArraySort(domainObject[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], CMDBuild.core.proxy.CMProxyConstants.INDEX);

						// Domain main node
						var domainMainNodeObject = { expandable: true, };
						domainMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
						domainMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = domainObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
						domainMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = root;
						domainMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.DOMAINS;

						var domainMainNode = root.appendChild(domainMainNodeObject);

						// Domain description property object
						var domainDescriptionNodeObject = {};
						domainDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = domainObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
						domainDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
						domainDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Description';
						domainDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = domainMainNode;
						domainDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;

						domainMainNode.appendChild(domainDescriptionNodeObject);

						// Domain direct description property object
						var domainDirectDescriptionNodeObject = {};
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = domainObject['descrdir'];
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Direct description';
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = domainMainNode;
						domainDirectDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.DIRECT_DESCRIPTION;

						domainMainNode.appendChild(domainDirectDescriptionNodeObject);

						// Domain inverse description property object
						var domainInverseDescriptionNodeObject = {};
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = domainObject['descrinv'];
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Inverse description';
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = domainMainNode;
						domainInverseDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.INVERSE_DESCRIPTION;

						domainMainNode.appendChild(domainInverseDescriptionNodeObject);

						// Domain master detail label property object
						if (Ext.isBoolean(domainObject['md']) && domainObject['md']) {
							var domainMasterDetailLabelNodeObject = {};
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = domainObject['md_label'];
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Master/Detail label';
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = domainMainNode;
							domainMasterDetailLabelNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = 'md_label';

							domainMainNode.appendChild(domainMasterDetailLabelNodeObject);
						}

						// Domain attributes node
						var domainAttributeNodeObject = { expandable: true };
						domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
						domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Attributes';
						domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = domainMainNode;
						domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES;

						var domainAttributesNode = domainMainNode.appendChild(domainAttributeNodeObject);

						// Domain attributes child node
						Ext.Array.forEach(domainObject[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], function(attributeObject, i, allAttributes) {
							var domainAttributeNodeObject = {};
							domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
							domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PARENT] = domainAttributesNode;
							domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE;
							domainAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];

							domainAttributesNode.appendChild(domainAttributeNodeObject);
						}, this);
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
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME] = this.getFirstLevelNode(node).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.FIELD] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.TRANSLATIONS] = Ext.encode(node.getChanges());
				localizationParams[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID] = (parentProperty == CMDBuild.core.proxy.CMProxyConstants.DOMAINS) ? this.getSectionId() : this.getSectionId() + CMDBuild.core.Utils.toTitleCase(parentProperty);

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