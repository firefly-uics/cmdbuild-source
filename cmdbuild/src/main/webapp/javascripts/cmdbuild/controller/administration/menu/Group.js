(function() {

	Ext.define('CMDBuild.controller.administration.menu.Group', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localizations.Localizations',
			'CMDBuild.core.proxy.Menu',
			'CMDBuild.model.menu.TreeStore'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.menu.Menu}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onMenuGroupAbortButtonClick',
			'onMenuGroupAddFolderButtonClick',
			'onMenuGroupBuildTreeStore',
			'onMenuGroupRemoveItemButtonClick',
			'onMenuGroupRemoveMenuButtonClick',
			'onMenuGroupSaveButtonClick'
		],

		/**
		 * @cfg {CMDBuild.view.administration.menu.group.GroupView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.menu.Menu} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.menu.group.GroupView', {
				delegate: this
			});
		},

		/**
		 * @param {Object} nodeObject
		 *
		 * @returns {Object} out
		 */
		buildNodeStructure: function(nodeObject) {
			var out = {};
			var superclass = false;
			var type = nodeObject[CMDBuild.core.proxy.Constants.TYPE];
			var folderType = nodeObject[CMDBuild.core.proxy.Constants.DESCRIPTION];

			if (
				(
					folderType == 'class'
					|| folderType == 'processclass'
				)
				&& _CMCache.isEntryTypeByName(nodeObject.referencedClassName)
			) {
				var entryType = _CMCache.getEntryTypeByName(nodeObject.referencedClassName);

				if (entryType)
					superclass = entryType.isSuperClass();
			}

			out.folderType = folderType;
			out.iconCls = 'cmdbuild-tree-' + (superclass ? 'super' : '') + type +'-icon';
			out.index = nodeObject.index;
			out.referencedClassName = nodeObject.referencedClassName;
			out.referencedElementId = nodeObject.referencedElementId;
			out.text = nodeObject[CMDBuild.core.proxy.Constants.DESCRIPTION];
			out.type = type;
			out.uuid = nodeObject.uuid;

			if (type == 'view')
				out.iconCls = 'cmdbuild-tree-class-icon';

			return out;
		},

		/**
		 * @param {Object} menuObject
		 *
		 * @returns {Object} out
		 */
		buildTreeStructure: function(menuObject) {
			var out = this.buildNodeStructure(menuObject);

			if (menuObject[CMDBuild.core.proxy.Constants.CHILDREN] || out[CMDBuild.core.proxy.Constants.TYPE] == 'folder') {
				out.leaf = false;
				out.children = [];
				out.expanded = false;

				var children = menuObject[CMDBuild.core.proxy.Constants.CHILDREN] || [];

				Ext.Array.forEach(children, function(childObject, i, allChildrenObjects) {
					out.children.push(this.buildTreeStructure(childObject));
				}, this);
			} else {
				out.leaf = true;
			}

			return out;
		},

		/**
		 * @param {CMDBuild.model.menu.TreeStore}
		 *
		 * @returns {Object}
		 */
		getMenuConfiguration: function(node) {
			var menuConfiguration = {};
			menuConfiguration['referencedClassName'] = node.get('referencedClassName');
			menuConfiguration['referencedElementId'] = node.get('referencedElementId');
			menuConfiguration['uuid'] = node.get('uuid');
			menuConfiguration[CMDBuild.core.proxy.Constants.DESCRIPTION] = node.get(CMDBuild.core.proxy.Constants.TEXT);
			menuConfiguration[CMDBuild.core.proxy.Constants.INDEX] = 0;
			menuConfiguration[CMDBuild.core.proxy.Constants.TYPE] = node.get(CMDBuild.core.proxy.Constants.TYPE);

			if (node.childNodes.length > 0) {
				menuConfiguration.children = [];

				Ext.Array.forEach(node.childNodes, function(childNode, i, allChildrenNodes) {
					childNode.set('parent', node.id);

					var childConf = this.getMenuConfiguration(childNode);
					childConf.index = i;

					menuConfiguration.children.push(childConf);
				}, this);
			}

			return menuConfiguration;
		},

		onMenuGroupAbortButtonClick: function() {
			this.onViewOnFront();
		},

		/**
		 * @param {String} folderName
		 */
		onMenuGroupAddFolderButtonClick: function(folderName) {
			if (!Ext.isEmpty(folderName)) {
				this.view.menuTreePanel.getRootNode().appendChild({
					text: folderName,
					type: 'folder',
					subtype: 'folder',
					iconCls: 'cmdbuild-tree-folder-icon',
					leaf: false
				});

				this.view.addFolderField.reset();
			}
		},

		/**
		 * @returns {Ext.data.TreeStore}
		 */
		onMenuGroupBuildTreeStore: function() {
			return Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.menu.TreeStore',

				root: {
					text: '',
					expanded: true,
					children: []
				}
			});
		},

		onMenuGroupRemoveItemButtonClick: function() {
			var selectedNode = this.view.menuTreePanel.getSelectionModel().getSelection()[0];

			if (!Ext.isEmpty(selectedNode) && !Ext.isEmpty(selectedNode.get(CMDBuild.core.proxy.Constants.TYPE)))
				this.removeTreeBranch(selectedNode);
		},

		onMenuGroupRemoveMenuButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes')
						this.removeItem();
				}
			});
		},

		onMenuGroupSaveButtonClick: function() {
			var menuTree = this.getMenuConfiguration(this.view.menuTreePanel.getRootNode());
			menuTree[CMDBuild.core.proxy.Constants.TYPE] = 'root';

			var params = {};
			params[CMDBuild.core.proxy.Constants.GROUP_NAME] = this.cmfg('selectedMenuNameGet');
			params[CMDBuild.core.proxy.Constants.MENU] = Ext.encode(menuTree);

			CMDBuild.core.proxy.Menu.save({
				params: params,
				scope: this,
				callback: function(response, options, decodedResponse) {
					this.onViewOnFront();

					// Customization of CMDBuild.view.common.field.translatable.Utils.commit
					Ext.Object.each(this.view.translatableAttributesConfigurationsBuffer, function(key, value, myself) {
						if (
							!Ext.isEmpty(value)
							&& !Ext.isEmpty(value[CMDBuild.core.proxy.Constants.TRANSLATIONS])
						) {
							value[CMDBuild.core.proxy.Constants.TRANSLATIONS] = Ext.encode(value[CMDBuild.core.proxy.Constants.TRANSLATIONS]);

							CMDBuild.core.proxy.localizations.Localizations.update({
								params: value,
								success: function(response, options, decodedResponse) {
									CMDBuild.core.Message.success();
								}
							});
						}
					}, this);
				}
			});
		},

		/**
		 * @override
		 */
		onViewOnFront: function() {
			var params = {};
			params[CMDBuild.core.proxy.Constants.GROUP_NAME] = this.cmfg('selectedMenuNameGet');

			CMDBuild.core.proxy.Menu.readConfiguration({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					var menu = this.buildTreeStructure(decodedResponse.menu);
					var root = this.view.menuTreePanel.getRootNode();

					root.removeAll();

					if (!Ext.isEmpty(menu[CMDBuild.core.proxy.Constants.CHILDREN])) // If empty has no children field
						root.appendChild(menu[CMDBuild.core.proxy.Constants.CHILDREN]);
				}
			});

			CMDBuild.core.proxy.Menu.readAvailableItems({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					var menu = this.buildTreeStructure(decodedResponse.menu);
					var root = this.view.availableItemsTreePanel.getRootNode();

					root.removeAll();
					root.appendChild(menu[CMDBuild.core.proxy.Constants.CHILDREN]);

					this.view.menuTreePanel.getStore().sort([
						{ property: CMDBuild.core.proxy.Constants.INDEX, direction: 'ASC' },
						{ property: CMDBuild.core.proxy.Constants.TEXT, direction: 'ASC' }
					]);
				}
			});
		},

		removeItem: function() {
			var params = {};
			params[CMDBuild.core.proxy.Constants.GROUP_NAME] = this.cmfg('selectedMenuNameGet');

			CMDBuild.core.proxy.Menu.remove({
				params: params,
				scope: this,
				callback: function(response, options, decodedResponse) {
					this.onViewOnFront();
				}
			});
		},

		removeTreeBranch: function(node) {
			while (node.hasChildNodes()) {
				this.removeTreeBranch(node.childNodes[0]);
			}

			var nodeType = node.get(CMDBuild.core.proxy.Constants.TYPE);

			if (nodeType.indexOf(CMDBuild.core.proxy.Constants.REPORT) >= 0)
				nodeType = 'report';

			var originalFolderOfTheLeaf = this.view.availableItemsTreePanel.getRootNode().findChild(CMDBuild.core.proxy.Constants.FOLDER_TYPE, nodeType);

			// Remove the node before adding it to the original tree
			node.remove();

			if (originalFolderOfTheLeaf) {
				originalFolderOfTheLeaf.expand();
				originalFolderOfTheLeaf.appendChild(node);
			}
		}
	});

})();