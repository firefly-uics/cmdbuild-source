(function() {

	Ext.define('CMDBuild.view.management.accordion.Menu', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.CustomPage',
			'CMDBuild.core.proxy.Menu',
			'CMDBuild.model.menu.accordion.Management'
		],

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * Used as a hack to get all customPages data from server
		 *
		 * @property {Array}
		 */
		customPagesResponse: [],

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.menu.accordion.Management',

		title: CMDBuild.Translation.navigation,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_NAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);
			params[CMDBuild.core.constants.Proxy.LOCALIZED] = true;

			CMDBuild.core.proxy.Menu.read({
				params: params,
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					menuItemsResponse = decodedResponse[CMDBuild.core.constants.Proxy.MENU];

					CMDBuild.core.proxy.CustomPage.readForCurrentUser({
						loadMask: false,
						scope: this,
						success: function(response, options, decodedResponse) {
							this.customPagesResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							if (
								!Ext.isEmpty(menuItemsResponse)
								&& !Ext.isEmpty(menuItemsResponse[CMDBuild.core.constants.Proxy.CHILDREN])
								&& Ext.isArray(menuItemsResponse[CMDBuild.core.constants.Proxy.CHILDREN])
								&& menuItemsResponse[CMDBuild.core.constants.Proxy.TYPE] == 'root'
							) {
								this.getStore().getRootNode().removeAll();
								this.getStore().getRootNode().appendChild(this.menuStructureChildrenBuilder(menuItemsResponse));
								this.getStore().sort();

								// Alias of this.callParent(arguments), inside proxy function doesn't work
								if (!Ext.isEmpty(this.delegate))
									this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
							}
						}
					});
				}
			});
		},

		/**
		 * @param {Object} menuObject - menu root node object
		 *
		 * @returns {Array} nodeStructure
		 */
		menuStructureChildrenBuilder: function(menuObject) {
			var nodeStructure = [];

			if (
				!Ext.isEmpty(menuObject[CMDBuild.core.constants.Proxy.CHILDREN])
				&& Ext.isArray(menuObject[CMDBuild.core.constants.Proxy.CHILDREN])
			) {
				Ext.Array.forEach(menuObject[CMDBuild.core.constants.Proxy.CHILDREN], function(childObject, i, allChildNodes) {
					nodeStructure.push(this.menuStructureNodeBuilder(childObject));
				}, this);
			}

			return nodeStructure;
		},

		/**
		 * @param {Object} menuNodeObject
		 * 	Ex. {
		 * 		{String} description
		 * 		{Number} index
		 * 		{String} referencedClassName
		 * 		{Number} referencedElementId
		 * 		{String} type - [class | processclass | dashboard | reportcsv | reportpdf | view]
		 * 	}
		 *
		 * @returns {Object} nodeStructure
		 */
		menuStructureNodeBuilder: function(menuNodeObject) {
			var nodeStructure = {};

			if (!Ext.Object.isEmpty(menuNodeObject)) {
				// Common attributes
				nodeStructure['cmIndex'] = menuNodeObject[CMDBuild.core.constants.Proxy.INDEX];
				nodeStructure[CMDBuild.core.constants.Proxy.DESCRIPTION] = menuNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
				nodeStructure[CMDBuild.core.constants.Proxy.TEXT] = menuNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
				nodeStructure[CMDBuild.core.constants.Proxy.LEAF] = true;

				switch (menuNodeObject[CMDBuild.core.constants.Proxy.TYPE]) {
					case 'class': {
						var entryType = _CMCache.getEntryTypeByName(menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME]);

						if (!Ext.isEmpty(entryType)) {
							nodeStructure['cmName'] = menuNodeObject[CMDBuild.core.constants.Proxy.TYPE];
							nodeStructure['iconCls'] = 'cmdbuild-tree-' + (entryType.isSuperClass() ? 'super' : '') + menuNodeObject[CMDBuild.core.constants.Proxy.TYPE] +'-icon';
							nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
							nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.FILTER];
							nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
								name: menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
								components: entryType.getId()
							});
							nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						}
					} break;

					/**
					 * Uses readForCurrentUser() server call response to get CustomPage name. Should be fixed with a server getAssignedMenu() call refactor.
					 */
					case 'custompage': {
						var customPageDataObject = Ext.Array.findBy(this.customPagesResponse, function(item, i) {
							return menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID] == item[CMDBuild.core.constants.Proxy.ID];
						}, this);

						nodeStructure['cmName'] = menuNodeObject[CMDBuild.core.constants.Proxy.TYPE];
						nodeStructure['iconCls'] = 'cmdbuild-tree-custompage-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
							name: menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
							components: menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						});
						nodeStructure[CMDBuild.core.constants.Proxy.NAME] = customPageDataObject[CMDBuild.core.constants.Proxy.NAME];
					} break;

					case 'dashboard': {
						nodeStructure['cmName'] = 'dashboard';
						nodeStructure['iconCls'] = 'cmdbuild-tree-dashboard-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
							name: menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
							components: menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID]
						});
					} break;

					case 'folder': {
						nodeStructure['cmName'] = 'folder';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = undefined; // Avoids node selection
						nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
					} break;

					case 'processclass': {
						var entryType = _CMCache.getEntryTypeByName(menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME]);

						if (!Ext.isEmpty(entryType)) {
							nodeStructure['cmName'] = 'workflow';
							nodeStructure['iconCls'] = 'cmdbuild-tree-' + (entryType.isSuperClass() ? 'super' : '') + menuNodeObject[CMDBuild.core.constants.Proxy.TYPE] +'-icon';
							nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
							nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.FILTER];
							nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
								name: menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
								components: entryType.getId()
							});
							nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						}
					} break;

					case 'reportcsv': {
						nodeStructure['cmName'] = 'singlereport';
						nodeStructure['iconCls'] = 'cmdbuild-tree-reportcsv-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
							name: 'singlereport',
							components: menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID]
						});
						nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = [CMDBuild.core.constants.Proxy.CSV];
					} break;

					case 'reportpdf': {
						nodeStructure['cmName'] = 'singlereport';
						nodeStructure['iconCls'] = 'cmdbuild-tree-reportpdf-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
							name: 'singlereport',
							components: menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID]
						});
						nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = [CMDBuild.core.constants.Proxy.PDF];
					} break;

					case 'view': {
						switch (menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.TYPE]) {
							case 'FILTER': {
								var entryTypeName = menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.SOURCE_CLASS_NAME];
								var entryType = _CMCache.getEntryTypeByName(entryTypeName);

								nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['filter'];

								if (!Ext.isEmpty(entryType)) {
									nodeStructure['cmName'] = 'class';
									nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
									nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.FILTER];
									nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
										name: 'dataview-filter',
										components: entryType.getId()
									});
								}
							} break;

							case 'SQL': { // TODO: check if fill with SQL or do something else
								nodeStructure['cmName'] = 'dataview';
								nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', {
									name: 'dataview-sql',
									components: menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
								});
								nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['sql'];
								nodeStructure[CMDBuild.core.constants.Proxy.SOURCE_FUNCTION] = menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.SOURCE_FUNCTION];
							} break;

							default: {
								_error(
									'specificTypeValues.type "'
									+ menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.TYPE]
									+ '" not managed',
									this
								);

								nodeStructure = {};
							}
						}
					} break;

					default: {
						_error('menu item type "' + menuNodeObject[CMDBuild.core.constants.Proxy.TYPE] + '" not managed', this);

						nodeStructure = {};
					}
				}

				// Build children nodes
				if (!Ext.isEmpty(menuNodeObject[CMDBuild.core.constants.Proxy.CHILDREN]) && Ext.isArray(menuNodeObject[CMDBuild.core.constants.Proxy.CHILDREN])) {
					nodeStructure[CMDBuild.core.constants.Proxy.CHILDREN] = this.menuStructureChildrenBuilder(menuNodeObject);
					nodeStructure[CMDBuild.core.constants.Proxy.LEAF] = false;
				}
			}

			return nodeStructure;
		}
	});

})();