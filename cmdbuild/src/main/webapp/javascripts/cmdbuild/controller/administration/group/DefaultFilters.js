(function() {

	Ext.define('CMDBuild.controller.administration.group.DefaultFilters', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.Server',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.group.DefaultFilters',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupAddButtonClick',
			'onGroupDefaultFiltersAbortButtonClick',
			'onGroupDefaultFiltersGroupSelected = onGroupGroupSelected',
			'onGroupDefaultFiltersSaveButtonClick',
			'onGroupDefaultFiltersTabShow',
			'onGroupDefaultFiltersTreeBeforeEdit'
		],

		/**
		 * Filters root class of all classes and root process of all processes
		 *
		 * @cfg {Array}
		 */
		filteredClasses: ['Activity', 'Class'],

		/**
		 * @property {CMDBuild.view.administration.group.defaultFilters.TreePanel}
		 */
		tree: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.group.defaultFilters.DefaultFiltersView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.group.Group} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.group.defaultFilters.DefaultFiltersView', { delegate: this });

			// Shorthands
			this.tree = this.view.tree;
		},

		/**
		 * Disable tab on add button click
		 */
		onGroupAddButtonClick: function() {
			this.view.disable();
		},

		onGroupDefaultFiltersAbortButtonClick: function() {
			if (!this.cmfg('selectedGroupIsEmpty'))
				this.onGroupDefaultFiltersTabShow();
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 */
		onGroupDefaultFiltersGroupSelected: function() {
			this.view.setDisabled(this.cmfg('selectedGroupIsEmpty'));
		},

		onGroupDefaultFiltersSaveButtonClick: function() {
			if (!this.cmfg('selectedGroupIsEmpty')) {
				var defaultFiltersNames = [];
				var defaultFiltersIds = [];
				var filterObjectsMap = {};

				var params = {};
				params[CMDBuild.core.constants.Proxy.LIMIT] = CMDBuild.core.constants.Server.MAX_INTEGER; // HACK to get all filters
				params[CMDBuild.core.constants.Proxy.START] = 0; // HACK to get all filters

				this.getAllDefaultFilters(this.tree.getStore().getRootNode(), defaultFiltersNames);

				// Translate filter name to ID
				CMDBuild.core.proxy.group.DefaultFilters.readAllGroupFilters({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse.filters;

						// Build filter object map ([{ name: filterObject, ... }])
						Ext.Array.forEach(decodedResponse, function(filterObject, i, allFilterObjects) {
							filterObjectsMap[filterObject[CMDBuild.core.constants.Proxy.NAME]] = filterObject;
						}, this);

						Ext.Array.forEach(defaultFiltersNames, function(defaultFilter, i, allDefaultFilters) {
							defaultFiltersIds.push(filterObjectsMap[defaultFilter][CMDBuild.core.constants.Proxy.ID]);
						}, this);

						params = {};
						params[CMDBuild.core.constants.Proxy.FILTERS] = Ext.encode(defaultFiltersIds);
						params[CMDBuild.core.constants.Proxy.GROUPS] = Ext.encode([this.cmfg('selectedGroupGet', CMDBuild.core.constants.Proxy.NAME)]);

						CMDBuild.core.proxy.group.DefaultFilters.update({ params: params });
					}
				});
			}
		},

		/**
		 * Builds tree store.
		 * Wrongly tableType attribute use to recognize three types of classes (standard, simple, processes).
		 */
		onGroupDefaultFiltersTabShow: function() {
			if (!this.cmfg('selectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				this.tree.getStore().getRootNode().removeAll();

				CMDBuild.core.proxy.Classes.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						var readedClasses = decodedResponse.classes;

						params = {};
						params[CMDBuild.core.constants.Proxy.GROUP] = this.cmfg('selectedGroupGet', CMDBuild.core.constants.Proxy.NAME);

						CMDBuild.core.proxy.group.DefaultFilters.read({
							params: params,
							scope: this,
							success: function(response, options, decodedResponse) {
								decodedResponse = decodedResponse.response.elements;

								var defaultFilters = {};
								var nodesMap = {};
								var processesTree = [];
								var simpleTree = [];
								var standardTree = [];

								Ext.Array.forEach(decodedResponse, function(filterObject, i, allFiltersObjects) {
									if (Ext.isEmpty(defaultFilters[filterObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE]]))
										defaultFilters[filterObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE]] = filterObject[CMDBuild.core.constants.Proxy.NAME];
								}, this);

								// Build all tree done objects
								Ext.Array.forEach(readedClasses, function(entityObject, i, allEntitiesObjects) {
									if (!Ext.Array.contains(this.filteredClasses, entityObject[CMDBuild.core.constants.Proxy.NAME])) { // Apply filter to classes
										switch(entityObject[CMDBuild.core.constants.Proxy.TYPE]) {
											case 'processclass': { // Process node object
												var processNodeObject = {};
												processNodeObject['iconCls'] = 'cmdbuild-tree-processclass-icon';
												processNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = entityObject[CMDBuild.core.constants.Proxy.TEXT];
												processNodeObject[CMDBuild.core.constants.Proxy.ID] = entityObject[CMDBuild.core.constants.Proxy.ID];
												processNodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
												processNodeObject[CMDBuild.core.constants.Proxy.NAME] = entityObject[CMDBuild.core.constants.Proxy.NAME];
												processNodeObject[CMDBuild.core.constants.Proxy.PARENT] = entityObject[CMDBuild.core.constants.Proxy.PARENT];
												processNodeObject[CMDBuild.core.constants.Proxy.TABLE_TYPE] = entityObject[CMDBuild.core.constants.Proxy.TYPE];

												// Preset node value
												if (!Ext.isEmpty(defaultFilters[entityObject[CMDBuild.core.constants.Proxy.NAME]]))
													processNodeObject[CMDBuild.core.constants.Proxy.DEFAULT_FILTER] = defaultFilters[entityObject[CMDBuild.core.constants.Proxy.NAME]];

												nodesMap[processNodeObject[CMDBuild.core.constants.Proxy.ID]] = processNodeObject;
											} break;

											case 'class':
											default: { // Class node object
												var classNodeObject = {};
												classNodeObject['iconCls'] = entityObject['superclass'] ? 'cmdbuild-tree-superclass-icon' : 'cmdbuild-tree-class-icon';
												classNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = entityObject[CMDBuild.core.constants.Proxy.TEXT];
												classNodeObject[CMDBuild.core.constants.Proxy.ID] = entityObject[CMDBuild.core.constants.Proxy.ID];
												classNodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
												classNodeObject[CMDBuild.core.constants.Proxy.NAME] = entityObject[CMDBuild.core.constants.Proxy.NAME];
												classNodeObject[CMDBuild.core.constants.Proxy.PARENT] = entityObject[CMDBuild.core.constants.Proxy.PARENT];
												classNodeObject[CMDBuild.core.constants.Proxy.TABLE_TYPE] = entityObject[CMDBuild.core.constants.Proxy.TABLE_TYPE];

												// Preset node value
												if (!Ext.isEmpty(defaultFilters[entityObject[CMDBuild.core.constants.Proxy.NAME]]))
													classNodeObject[CMDBuild.core.constants.Proxy.DEFAULT_FILTER] = defaultFilters[entityObject[CMDBuild.core.constants.Proxy.NAME]];

												nodesMap[classNodeObject[CMDBuild.core.constants.Proxy.ID]] = classNodeObject;
											}
										}
									}
								}, this);

								// Builds full standard/simple/process classes trees
								Ext.Object.each(nodesMap, function(id, node, myself) {
									switch(node[CMDBuild.core.constants.Proxy.TABLE_TYPE]) {
										case CMDBuild.core.constants.Proxy.STANDARD: {
											if (
												!Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])
												&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]])
											) {
												var parentNode = nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];

												parentNode.children = (parentNode.children || []);
												parentNode[CMDBuild.core.constants.Proxy.LEAF] = false;
												parentNode.children.push(node);
											} else {
												standardTree.push(node);
											}
										} break;

										case 'processclass': {
											if (
												!Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])
												&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]])
											) {
												var parentNode = nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];

												parentNode.children = (parentNode.children || []);
												parentNode[CMDBuild.core.constants.Proxy.LEAF] = false;
												parentNode.children.push(node);
											} else {
												processesTree.push(node);
											}
										} break;

										case 'simpletable':
										default: {
											simpleTree.push(node);
										}
									}
								}, this);

								// Alphabetical sorting
								CMDBuild.core.Utils.objectArraySort(standardTree);
								CMDBuild.core.Utils.objectArraySort(simpleTree);

								this.tree.getStore().getRootNode().appendChild([
									{
										description: CMDBuild.Translation.standard,
										leaf: false,
										children: standardTree,
										expanded: true
									},
									{
										description: CMDBuild.Translation.simple,
										leaf: false,
										children: simpleTree,
										expanded: true
									},
									{
										description: CMDBuild.Translation.processes,
										leaf: false,
										children: processesTree,
										expanded: true
									}
								]);
							}
						});
					}
				});
			}
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Boolean}
		 */
		onGroupDefaultFiltersTreeBeforeEdit: function(parameters) {
			var colIdx = parameters.colIdx;
			var column = parameters.column;
			var record = parameters.record;

			if (
				colIdx == 1 // Avoid to go in edit of unwanted columns
				&& record.getDepth() > 1// Avoid to go in edit of root classes (standard and simple)
			) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.LIMIT] = CMDBuild.core.constants.Server.MAX_INTEGER; // HACK to get all filters
				params[CMDBuild.core.constants.Proxy.START] = 0; // HACK to get all filters

				column.getEditor().getStore().load({ params: params });

				return true;
			}

			return false;
		},


		/**
		 * @param {Object} node
		 * @param {Array} destinationArray
		 */
		getAllDefaultFilters: function(node, destinationArray) {
			node.eachChild(function(childNode) {
				if (!Ext.isEmpty(childNode.get(CMDBuild.core.constants.Proxy.DEFAULT_FILTER)))
					destinationArray.push(childNode.get(CMDBuild.core.constants.Proxy.DEFAULT_FILTER));

				if (!Ext.isEmpty(node.hasChildNodes()))
					this.getAllDefaultFilters(childNode, destinationArray);
			}, this);
		}
	});

})();