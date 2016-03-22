(function() {

	Ext.define('CMDBuild.controller.administration.navigationTree.Tree', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.domain.Domain',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.NavigationTree}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationTreeTabTreeDataGet',
			'onNavigationTreeTabTreeAbortButtonClick',
			'onNavigationTreeTabTreeAddButtonClick',
			'onNavigationTreeTabTreeCheckChange',
			'onNavigationTreeTabTreeModifyButtonClick',
			'onNavigationTreeTabTreeNodeExpand',
			'onNavigationTreeTabTreeSelected = onNavigationTreeSelected'
		],

		/**
		 * @property {CMDBuild.view.administration.navigationTree.tree.FormPanel}
		 */
		form: undefined,

		/**
		 * Local cache used to avoid too many server calls
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		localCache: {
			entryTypes: {}, // NAME as key
			domains: {} // ID_DOMAIN as key
		},

		/**
		 * @property {CMDBuild.view.administration.navigationTree.tree.TreePanel}
		 */
		tree: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.navigationTree.tree.TreeView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.navigationTree.NavigationTree} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.navigationTree.tree.TreeView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.tree = this.form.tree;
		},

		/**
		 * Build tree nodes
		 *
		 * @param {Number} subjectEntryTypeId
		 * @param {String} subjectEntryTypeName
		 * @param {Number} excludeDomainId
		 *
		 * @returns {Array} nodes
		 *
		 * @private
		 */
		buildNodesArray: function (subjectEntryTypeId, subjectEntryTypeName, excludeDomainId) {
			var nodes = [];

			if (
				!Ext.isEmpty(subjectEntryTypeId) && Ext.isNumber(subjectEntryTypeId)
				&& !Ext.isEmpty(subjectEntryTypeName) && Ext.isString(subjectEntryTypeName)
			) {
				var ancestorsDomains = this.getDomainsWithEntryType(subjectEntryTypeId, excludeDomainId);

				if (!Ext.isEmpty(ancestorsDomains) && Ext.isArray(ancestorsDomains)) {
					Ext.Array.each(ancestorsDomains, function (anchestorDomainModel, i, allAnchestorDomainModels) {
						if (!Ext.isEmpty(anchestorDomainModel)) {
							var oppositeEntryTypeModel = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME);
							var domainDescription = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION);

							if (subjectEntryTypeName == anchestorDomainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME)) {
								oppositeEntryTypeModel = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME);
								domainDescription = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION);
							}

							oppositeEntryTypeModel = this.localCacheEntryTypeGet(oppositeEntryTypeModel); // Get full model

							// Build node object
							var childObject = {
								children: [{}], // Fake node to enable node expand
								checked: false, // Enables checkbox
								expandable: true,
								leaf: false
							};
							childObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
								+ ' (' + domainDescription + ' ' + oppositeEntryTypeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION) + ')';
							childObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = oppositeEntryTypeModel;
							childObject[CMDBuild.core.constants.Proxy.DOMAIN] = anchestorDomainModel;

							nodes.push(childObject);
						}
					}, this);
				}
			} else {
				_error('wrong or malformed buildNodesArray method parameters', this);
			}

			return nodes;
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @returns {Array} checkedChildren
		 *
		 * @private
		 */
		getCheckedChild: function (node) {
			var checkedChildren = [];

			if (!Ext.isEmpty(node) && node.hasChildNodes())
				node.eachChild(function (childNode) {
					if (!Ext.isEmpty(childNode) && childNode.get('checked')) {
						var domainModel = childNode.get(CMDBuild.core.constants.Proxy.DOMAIN);
						var entryTypeModel = childNode.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE);

						var childObject = { direct: true };
						childObject[CMDBuild.core.constants.Proxy.BASE_NODE] = childNode.isRoot();
						childObject[CMDBuild.core.constants.Proxy.CHILD_NODES] = this.getCheckedChild(childNode);
						childObject[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = domainModel.get(CMDBuild.core.constants.Proxy.NAME);
						childObject[CMDBuild.core.constants.Proxy.ENABLE_RECURSION] = childNode.get(CMDBuild.core.constants.Proxy.ENABLE_RECURSION);
						childObject[CMDBuild.core.constants.Proxy.FILTER] = childNode.get(CMDBuild.core.constants.Proxy.FILTER);
						childObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION] = entryTypeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION);
						childObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME] = entryTypeModel.get(CMDBuild.core.constants.Proxy.NAME);

						checkedChildren.push(childObject);
					}
				}, this);

			return checkedChildren;
		},

		/**
		 * Get all domains where origin or destinations is entryTypeId (also filters excludeDomainId)
		 *
		 * @param {Number} entryTypeId
		 * @param {String} excludeDomainName
		 *
		 * @returns {Array} ancestorsDomains
		 *
		 * @private
		 */
		getDomainsWithEntryType: function (entryTypeId, excludeDomainId) {
			excludeDomainId = Ext.isNumber(excludeDomainId) ? excludeDomainId : null;

			if (!Ext.isEmpty(entryTypeId) && Ext.isNumber(entryTypeId)) {
				var ancestorsDomains = [];
				var ancestorsId = _CMUtils.getAncestorsId(entryTypeId);

				// Cast string to number
				if (!Ext.isEmpty(ancestorsId) && Ext.isArray(ancestorsId))
					Ext.Array.each(ancestorsId, function (id, i, allId) {
						if (!Ext.isEmpty(id) && Ext.isString(id))
							ancestorsId[i] = parseInt(id);
					}, this);

				// Retrieve selectedClass related domains
				if (!Ext.isEmpty(ancestorsId) && Ext.isArray(ancestorsId))
					this.localCacheDomainEach(function (id, model, myself) {
						if (
							model.get(CMDBuild.core.constants.Proxy.ID) != excludeDomainId
							&& (
								Ext.Array.contains(ancestorsId, model.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID))
								|| Ext.Array.contains(ancestorsId, model.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID))
							)
						) {
							ancestorsDomains.push(model);
						}
					}, this);

				return ancestorsDomains;
			} else {
				_error('wrong or malformed getDomainsWithEntryType method parameters', this);
			}
		},

		/**
		 * Walk through tree and stateObject to fill tree nodes with check values and filters
		 *
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 * @param {Array} stateObjectArray
		 *
		 * @private
		 */
		loadNodesCheckState: function (node, stateObjectArray) {
			if (
				!Ext.isEmpty(node)
				&& !Ext.isEmpty(stateObjectArray) && Ext.isArray(stateObjectArray)
			) {
				node.expand();

				Ext.Array.each(stateObjectArray, function (stateObject, i, allStateObjects) {
					if (Ext.isObject(stateObject) && !Ext.Object.isEmpty(stateObject)) {
						var soughtNode = node.findChildBy(function (childNode) {
							var domainModel = childNode.get(CMDBuild.core.constants.Proxy.DOMAIN);
							var entryTypeModel = childNode.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE);

							return (
								domainModel.get(CMDBuild.core.constants.Proxy.NAME) == stateObject[CMDBuild.core.constants.Proxy.DOMAIN_NAME]
								&& entryTypeModel.get(CMDBuild.core.constants.Proxy.NAME) == stateObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME]
								&& entryTypeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION) == stateObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION]
							);
						}, this);

						if (!Ext.isEmpty(soughtNode)) {
							soughtNode.set('checked', true);
							soughtNode.set(CMDBuild.core.constants.Proxy.ENABLE_RECURSION, stateObject[CMDBuild.core.constants.Proxy.ENABLE_RECURSION]);
							soughtNode.set(CMDBuild.core.constants.Proxy.FILTER, stateObject[CMDBuild.core.constants.Proxy.FILTER]);

							this.loadNodesCheckState(soughtNode, stateObject[CMDBuild.core.constants.Proxy.CHILD_NODES]);
						}
					}
				}, this);
			}
		},

		// Domain local cache methods
			/**
			 * @param {Function} callback
			 * @param {Object} scope
			 *
			 * @private
			 */
			localCacheDomainEach: function (callback, scope) {
				if (Ext.isFunction(callback))
					Ext.Object.each(this.localCache.domains, callback, scope);
			},

			/**
			 * @param {Number} domainId
			 *
			 * @returns {Mixed}
			 *
			 * @private
			 */
			localCacheDomainGet: function (domainId) {
				if (!Ext.isEmpty(domainId) && Ext.isString(domainId))
					return this.localCache.domains[domainId];

				return null;
			},

			/**
			 * @param {Array} domainsArray
			 *
			 * @private
			 */
			localCacheDomainSet: function (domainsArray) {
				if (!Ext.isEmpty(domainsArray) && Ext.isArray(domainsArray))
					Ext.Array.each(domainsArray, function (domainObject, i, allDomainObjects) {
						if (Ext.isObject(domainObject) && !Ext.Object.isEmpty(domainObject))
							this.localCache.domains[domainObject[CMDBuild.core.constants.Proxy.ID_DOMAIN]] = Ext.create('CMDBuild.model.navigationTree.Domain', domainObject);
					}, this);
			},

		// EntryType local cache methods
			/**
			 * @param {String} entryTypeName
			 *
			 * @returns {Mixed}
			 *
			 * @private
			 */
			localCacheEntryTypeGet: function (entryTypeName, attributeName) {
				if (!Ext.isEmpty(entryTypeName) && Ext.isString(entryTypeName))
					if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName)) {
						return this.localCache.entryTypes[entryTypeName].get(attributeName);
					} else {
						return this.localCache.entryTypes[entryTypeName];
					}

				return null;
			},

			/**
			 * @param {Array} classesArray
			 *
			 * @private
			 */
			localCacheEntryTypeSet: function (classesArray) {
				if (!Ext.isEmpty(classesArray) && Ext.isArray(classesArray))
					Ext.Array.each(classesArray, function (entryTypeObject, i, allEntryTyopeObjects) {
						if (Ext.isObject(entryTypeObject) && !Ext.Object.isEmpty(entryTypeObject))
							this.localCache.entryTypes[entryTypeObject[CMDBuild.core.constants.Proxy.NAME]] = Ext.create('CMDBuild.model.navigationTree.Class', entryTypeObject);
					}, this);
			},

		/**
		 * @param {string} type
		 *
		 * @returns {Mixed}
		 */
		navigationTreeTabTreeDataGet: function (type) {
			switch (type) {
				case 'childNodes': // Returns root's child nodes array
					return this.getCheckedChild(this.tree.getStore().getRootNode());

				case 'rootFilter': // Returns root's data
					return this.tree.getStore().getRootNode().get(CMDBuild.core.constants.Proxy.FILTER);

				default:
					return null;
			}
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @private
		 */
		onExpandNode: function (node) {
			if (!Ext.isEmpty(node) && Ext.isObject(node)) {
				var nodeDomainModel = node.get(CMDBuild.core.constants.Proxy.DOMAIN);
				var nodes = this.buildNodesArray( // Build domain's origin nodes
					nodeDomainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID),
					nodeDomainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME),
					nodeDomainModel.get(CMDBuild.core.constants.Proxy.ID)
				);

				if (!Ext.isEmpty(nodes)) {
					CMDBuild.core.Utils.objectArraySort(nodes, CMDBuild.core.constants.Proxy.DESCRIPTION);

					node.removeAll();
					node.appendChild(nodes);
				}
			} else {
				_error('wrong or malformed onExpandNode method parameters', this);
			}
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @private
		 */
		onExpandRootNode: function (node) {
			if (!Ext.isEmpty(node) && Ext.isObject(node)) {
				var targetClassName = node.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE).get(CMDBuild.core.constants.Proxy.NAME);
				var nodes = this.buildNodesArray(
					this.localCacheEntryTypeGet(targetClassName, CMDBuild.core.constants.Proxy.ID),
					targetClassName
				);

				if (!Ext.isEmpty(nodes)) {
					CMDBuild.core.Utils.objectArraySort(nodes, CMDBuild.core.constants.Proxy.DESCRIPTION);

					node.removeAll();
					node.appendChild(nodes);
				}
			} else {
				_error('wrong or malformed onExpandRootNode method parameters', this);
			}
		},

		onNavigationTreeTabTreeAbortButtonClick: function () {
			if (this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				this.tree.getStore().getRootNode().removeAll();
				this.tree.getSelectionModel().deselectAll();

				this.form.setDisabledModify(true, true, true);
			} else {
				this.cmfg('onNavigationTreeTabTreeSelected');
			}
		},

		onNavigationTreeTabTreeAddButtonClick: function () {
			this.view.setDisabled(true);

			this.tree.getStore().getRootNode().removeAll();
		},

		/**
		 * @param {Object} parameters
		 * @param {CMDBuild.model.navigationTree.TreeNode} parameters.node
		 * @param {Boolean} parameters.checked
		 */
		onNavigationTreeTabTreeCheckChange: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.node)
			) {
				parameters.checked = Ext.isBoolean(parameters.checked) ? parameters.checked : false;

				var node = parameters.node;

				if (parameters.checked) {
					while (!Ext.isEmpty(node.parentNode)) {
						node.set('checked', true);
						node = node.parentNode;
					}
				} else {
					this.unCheckChildNodes(node);
				}
			} else {
				_error('wrong or malformed onNavigationTreeTabTreeCheckChange method parameters', this);
			}
		},

		onNavigationTreeTabTreeModifyButtonClick: function () {
			this.form.setDisabledModify(false);
			this.form.setDisableFields(false, false, true); // To enable also if not visible

			this.tree.getView().refresh(); // Fixes enable/disable checkcolumn problems
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 */
		onNavigationTreeTabTreeNodeExpand: function (node) {
			if (!Ext.isEmpty(node)) {
				if (node.getDepth() == 0) { // Expand root node
					this.onExpandRootNode(node);
				} else { // Expand other nodes
					this.onExpandNode(node);
				}
			} else {
				_error('wrong or malformed onNavigationTreeTabTreeNodeExpand method parameters', this);
			}
		},

		/**
		 * Build root node and Classes/Domains local cache to avoid too many server calls
		 */
		onNavigationTreeTabTreeSelected: function () {
			this.view.setDisabled(this.cmfg('navigationTreeSelectedTreeIsEmpty'));

			this.tree.getStore().getRootNode().removeAll();

			if (this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				this.form.setDisabledModify(true, true, true, true);
			} else {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false; // Also inactive to get all processes if shark isn't on

				CMDBuild.core.proxy.Classes.read({ // TODO: waiting for refactor (CRUD)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (!Ext.isEmpty(decodedResponse)) {
							this.localCacheEntryTypeSet(decodedResponse);

							var targetClassModel = this.localCacheEntryTypeGet(this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME));

							// Build root node
							var rootNodeObject = {
								children: [{}],
								checked: true,
								expandable: true,
								expanded: false,
								leaf: false
							};
							rootNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = targetClassModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION);
							rootNodeObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = targetClassModel;

							this.tree.getStore().setRootNode(rootNodeObject);

							CMDBuild.core.proxy.domain.Domain.readAll({
								scope: this,
								success: function (response, options, decodedResponse) {
									decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

									if (!Ext.isEmpty(decodedResponse)) {
										this.localCacheDomainSet(decodedResponse);

										this.form.setDisabledModify(true);

										if (!this.cmfg('navigationTreeSelectedTreeIsEmpty', CMDBuild.core.constants.Proxy.CHILD_NODES))
											this.tree.getStore().getRootNode().set( // Setup root node filter
												CMDBuild.core.constants.Proxy.FILTER,
												this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.FILTER)
											);

										this.loadNodesCheckState(
											this.tree.getStore().getRootNode(),
											this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.CHILD_NODES)
										);
									}
								}
							});
						}
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @private
		 */
		unCheckChildNodes: function (node) {
			if (!Ext.isEmpty(node) && Ext.isFunction(node.hasChildNodes) && node.hasChildNodes())
				node.eachChild(function (childNode) {
					childNode.set('checked', false);

					this.unCheckChildNodes(childNode);
				}, this);
		}
	});

})();
