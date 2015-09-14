(function() {

	Ext.define('CMDBuild.controller.administration.group.DefaultFilters', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Server',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Constants',
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
				params[CMDBuild.core.proxy.Constants.LIMIT] = CMDBuild.core.constants.Server.getMaxInteger(); // HACK to get all filters
				params[CMDBuild.core.proxy.Constants.START] = 0; // HACK to get all filters

				this.getAllDefaultFilters(this.tree.getStore().getRootNode(), defaultFiltersNames);

				// Translate filter name to ID
				CMDBuild.core.proxy.group.DefaultFilters.readAllGroupFilters({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse.filters;

						// Build filter object map ([{ name: filterObject, ... }])
						Ext.Array.forEach(decodedResponse, function(filterObject, i, allFilterObjects) {
							filterObjectsMap[filterObject[CMDBuild.core.proxy.Constants.NAME]] = filterObject;
						}, this);

						Ext.Array.forEach(defaultFiltersNames, function(defaultFilter, i, allDefaultFilters) {
							defaultFiltersIds.push(filterObjectsMap[defaultFilter][CMDBuild.core.proxy.Constants.ID]);
						}, this);

						params = {};
						params[CMDBuild.core.proxy.Constants.FILTERS] = Ext.encode(defaultFiltersIds);
						params[CMDBuild.core.proxy.Constants.GROUPS] = Ext.encode([this.cmfg('selectedGroupGet', CMDBuild.core.proxy.Constants.NAME)]);

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
				params[CMDBuild.core.proxy.Constants.ACTIVE] = true;

				this.tree.getStore().getRootNode().removeAll();

				CMDBuild.core.proxy.Classes.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						var readedClasses = decodedResponse.classes;

						params = {};
						params[CMDBuild.core.proxy.Constants.GROUP] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.Constants.NAME);

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
									if (Ext.isEmpty(defaultFilters[filterObject[CMDBuild.core.proxy.Constants.ENTRY_TYPE]]))
										defaultFilters[filterObject[CMDBuild.core.proxy.Constants.ENTRY_TYPE]] = filterObject[CMDBuild.core.proxy.Constants.NAME];
								}, this);

								// Build all tree done objects
								Ext.Array.forEach(readedClasses, function(entityObject, i, allEntitiesObjects) {
									if (!Ext.Array.contains(this.filteredClasses, entityObject[CMDBuild.core.proxy.Constants.NAME])) { // Apply filter to classes
										switch(entityObject[CMDBuild.core.proxy.Constants.TYPE]) {
											case 'processclass': { // Process node object
												var processNodeObject = {};
												processNodeObject['iconCls'] = 'cmdbuild-tree-processclass-icon';
												processNodeObject[CMDBuild.core.proxy.Constants.DESCRIPTION] = entityObject[CMDBuild.core.proxy.Constants.TEXT];
												processNodeObject[CMDBuild.core.proxy.Constants.ID] = entityObject[CMDBuild.core.proxy.Constants.ID];
												processNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
												processNodeObject[CMDBuild.core.proxy.Constants.NAME] = entityObject[CMDBuild.core.proxy.Constants.NAME];
												processNodeObject[CMDBuild.core.proxy.Constants.PARENT] = entityObject[CMDBuild.core.proxy.Constants.PARENT];
												processNodeObject[CMDBuild.core.proxy.Constants.TABLE_TYPE] = entityObject[CMDBuild.core.proxy.Constants.TYPE];

												// Preset node value
												if (!Ext.isEmpty(defaultFilters[entityObject[CMDBuild.core.proxy.Constants.NAME]]))
													processNodeObject[CMDBuild.core.proxy.Constants.DEFAULT_FILTER] = defaultFilters[entityObject[CMDBuild.core.proxy.Constants.NAME]];

												nodesMap[processNodeObject[CMDBuild.core.proxy.Constants.ID]] = processNodeObject;
											} break;

											case 'class':
											default: { // Class node object
												var classNodeObject = {};
												classNodeObject['iconCls'] = entityObject['superclass'] ? 'cmdbuild-tree-superclass-icon' : 'cmdbuild-tree-class-icon';
												classNodeObject[CMDBuild.core.proxy.Constants.DESCRIPTION] = entityObject[CMDBuild.core.proxy.Constants.TEXT];
												classNodeObject[CMDBuild.core.proxy.Constants.ID] = entityObject[CMDBuild.core.proxy.Constants.ID];
												classNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
												classNodeObject[CMDBuild.core.proxy.Constants.NAME] = entityObject[CMDBuild.core.proxy.Constants.NAME];
												classNodeObject[CMDBuild.core.proxy.Constants.PARENT] = entityObject[CMDBuild.core.proxy.Constants.PARENT];
												classNodeObject[CMDBuild.core.proxy.Constants.TABLE_TYPE] = entityObject[CMDBuild.core.proxy.Constants.TABLE_TYPE];

												// Preset node value
												if (!Ext.isEmpty(defaultFilters[entityObject[CMDBuild.core.proxy.Constants.NAME]]))
													classNodeObject[CMDBuild.core.proxy.Constants.DEFAULT_FILTER] = defaultFilters[entityObject[CMDBuild.core.proxy.Constants.NAME]];

												nodesMap[classNodeObject[CMDBuild.core.proxy.Constants.ID]] = classNodeObject;
											}
										}
									}
								}, this);

								// Builds full standard/simple/process classes trees
								Ext.Object.each(nodesMap, function(id, node, myself) {
									switch(node[CMDBuild.core.proxy.Constants.TABLE_TYPE]) {
										case CMDBuild.core.proxy.Constants.STANDARD: {
											if (
												!Ext.isEmpty(node[CMDBuild.core.proxy.Constants.PARENT])
												&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]])
											) {
												var parentNode = nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]];

												parentNode.children = (parentNode.children || []);
												parentNode[CMDBuild.core.proxy.Constants.LEAF] = false;
												parentNode.children.push(node);
											} else {
												standardTree.push(node);
											}
										} break;

										case 'processclass': {
											if (
												!Ext.isEmpty(node[CMDBuild.core.proxy.Constants.PARENT])
												&& !Ext.isEmpty(nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]])
											) {
												var parentNode = nodesMap[node[CMDBuild.core.proxy.Constants.PARENT]];

												parentNode.children = (parentNode.children || []);
												parentNode[CMDBuild.core.proxy.Constants.LEAF] = false;
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
				params[CMDBuild.core.proxy.Constants.CLASS_NAME] = record.get(CMDBuild.core.proxy.Constants.NAME);
				params[CMDBuild.core.proxy.Constants.LIMIT] = CMDBuild.core.constants.Server.getMaxInteger(); // HACK to get all filters
				params[CMDBuild.core.proxy.Constants.START] = 0; // HACK to get all filters

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
				if (!Ext.isEmpty(childNode.get(CMDBuild.core.proxy.Constants.DEFAULT_FILTER)))
					destinationArray.push(childNode.get(CMDBuild.core.proxy.Constants.DEFAULT_FILTER));

				if (!Ext.isEmpty(node.hasChildNodes()))
					this.getAllDefaultFilters(childNode, destinationArray);
			}, this);
		}
	});

})();