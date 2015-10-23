(function() {

	Ext.define('CMDBuild.view.management.accordion.Menu', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Menu',
			'CMDBuild.model.common.accordion.Menu'
		],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Menu}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		delegateClassName: 'CMDBuild.controller.management.accordion.Menu',

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Menu',

		title: CMDBuild.Translation.navigation,

		/**
		 * Generates an unique id for the menu accordion
		 *
		 * @param {Array} components
		 *
		 * @return {String}
		 */
		buildCustomId: function(components) {
			components = Ext.isArray(components) ? Ext.Array.clean(components) : [components];

			if (!Ext.isEmpty(components)) {
				Ext.Array.forEach(components, function(component, i, allComponents) {
					components[i] = Ext.String.trim(String(component));
				}, this);

				return this.cmName + '-' + components.join('-');
			}

			return this.cmName + '-' + Date.now();
		},

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
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.MENU];

					if (
						!Ext.isEmpty(decodedResponse)
						&& !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.CHILDREN])
						&& Ext.isArray(decodedResponse[CMDBuild.core.constants.Proxy.CHILDREN])
						&& decodedResponse[CMDBuild.core.constants.Proxy.TYPE] == 'root'
					) {
						this.getStore().getRootNode().removeAll();

						this.getStore().getRootNode().appendChild(this.menuStructureChildrenBuilder(decodedResponse));

						this.getStore().sort();

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						if (!Ext.isEmpty(this.delegate))
							this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
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
							nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
								menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
								entryType.getId(),
								menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
							]);
							nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						}
					} break;

					case 'custompage': {
						nodeStructure['cmName'] = menuNodeObject[CMDBuild.core.constants.Proxy.TYPE];
						nodeStructure['iconCls'] = 'cmdbuild-tree-custompage-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
							menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
					} break;

					case 'dashboard': {
						nodeStructure['cmName'] = 'dashboard';
						nodeStructure['iconCls'] = 'cmdbuild-tree-dashboard-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
							'dashboard',
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
					} break;

					case 'folder': {
						nodeStructure['cmName'] = 'folder';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
							'folder',
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
						nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
					} break;

					case 'processclass': {
						var entryType = _CMCache.getEntryTypeByName(menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME]);

						if (!Ext.isEmpty(entryType)) {
							nodeStructure['cmName'] = 'process'; // TODO: waiting for refactor (compatibility hack)
							nodeStructure['iconCls'] = 'cmdbuild-tree-' + (entryType.isSuperClass() ? 'super' : '') + menuNodeObject[CMDBuild.core.constants.Proxy.TYPE] +'-icon';
							nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
							nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.FILTER];
							nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
								'process',
								entryType.getId(),
								menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
							]);
							nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						}
					} break;

					case 'reportcsv': {
						nodeStructure['cmName'] = 'singlereport';
						nodeStructure['iconCls'] = 'cmdbuild-tree-reportcsv-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
							'singlereport',
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
						nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = [CMDBuild.core.constants.Proxy.CSV];
					} break;

					case 'reportpdf': {
						nodeStructure['cmName'] = 'singlereport';
						nodeStructure['iconCls'] = 'cmdbuild-tree-reportpdf-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
							'singlereport',
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
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
									nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
										'dataview',
										'filter',
										entryType.getId(),
										menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
									]);
								}
							} break;

							case 'SQL': { // TODO: check if fill with SQL or do something else
								nodeStructure['cmName'] = 'dataview';
								nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.buildCustomId([
									'dataview',
									'sql',
									menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
								]);
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