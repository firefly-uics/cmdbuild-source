(function () {

	Ext.define('CMDBuild.controller.administration.menu.Group', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.localization.Localization',
			'CMDBuild.core.proxy.Menu'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.menu.Menu}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Ext.tree.Panel}
		 */
		availableItemsTreePanel: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onMenuGroupAbortButtonClick',
			'onMenuGroupAddFolderButtonClick',
			'onMenuGroupMenuSelected',
			'onMenuGroupMenuTreeBeforeselect',
			'onMenuGroupMenuTreeSelectionchange',
			'onMenuGroupRemoveItemButtonClick',
			'onMenuGroupRemoveMenuButtonClick',
			'onMenuGroupSaveButtonClick'
		],

		/**
		 * @property {Ext.tree.Panel}
		 */
		menuTreePanel: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.MoveRight}
		 */
		removeItemButton: undefined,

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
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.menu.group.GroupView', { delegate: this });

			// Shorthands
			this.availableItemsTreePanel = this.view.availableItemsTreePanel;
			this.menuTreePanel = this.view.menuTreePanel;
			this.removeItemButton = this.view.removeItemButton;
		},

		/**
		 * @param {Object} nodeObject
		 *
		 * @returns {Object} out
		 */
		buildNodeStructure: function (nodeObject) {
			var out = {};
			var superclass = false;
			var type = nodeObject[CMDBuild.core.constants.Proxy.TYPE];
			var folderType = nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];

			if (
				(
					type == CMDBuild.core.constants.Global.getTableTypeClass()
					|| type == CMDBuild.core.constants.Global.getTableTypeProcessClass()
				)
				&& _CMCache.isEntryTypeByName(nodeObject.referencedClassName)
			) {
				var entryType = _CMCache.getEntryTypeByName(nodeObject.referencedClassName);

				if (entryType)
					superclass = entryType.isSuperClass();
			}

			out.folderType = folderType;
			out.iconCls = 'cmdb-tree-' + (superclass ? 'super' : '') + type +'-icon';
			out.index = nodeObject.index;
			out.referencedClassName = nodeObject.referencedClassName;
			out.referencedElementId = nodeObject.referencedElementId;
			out.type = type;
			out.folderType = nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
			out.uuid = nodeObject.uuid;

			// Label translation
			switch (nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION]) {
				case 'class': {
					out.text = CMDBuild.Translation.classes;
				} break;

				case 'custompage': {
					out.text = CMDBuild.Translation.customPages;
				} break;

				case 'dashboard': {
					out.text = CMDBuild.Translation.dashboard;
				} break;

				case 'processclass': {
					out.text = CMDBuild.Translation.processes;
				} break;

				case 'report': {
					out.text = CMDBuild.Translation.report;
				} break;

				case 'view': {
					out.text = CMDBuild.Translation.views;
				} break;

				default: {
					out.text = nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
				}
			}

			return out;
		},

		/**
		 * @param {Object} menuObject
		 *
		 * @returns {Object} out
		 */
		buildTreeStructure: function (menuObject) {
			var out = this.buildNodeStructure(menuObject);

			if (menuObject[CMDBuild.core.constants.Proxy.CHILDREN] || out[CMDBuild.core.constants.Proxy.TYPE] == 'folder') {
				out.leaf = false;
				out.children = [];
				out.expanded = false;

				var children = menuObject[CMDBuild.core.constants.Proxy.CHILDREN] || [];

				Ext.Array.forEach(children, function (childObject, i, allChildrenObjects) {
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
		getMenuConfiguration: function (node) {
			var menuConfiguration = {};
			menuConfiguration['referencedClassName'] = node.get('referencedClassName');
			menuConfiguration['referencedElementId'] = node.get('referencedElementId');
			menuConfiguration['uuid'] = node.get('uuid');
			menuConfiguration[CMDBuild.core.constants.Proxy.DESCRIPTION] = node.get(CMDBuild.core.constants.Proxy.TEXT);
			menuConfiguration[CMDBuild.core.constants.Proxy.INDEX] = 0;
			menuConfiguration[CMDBuild.core.constants.Proxy.TYPE] = node.get(CMDBuild.core.constants.Proxy.TYPE);

			if (node.childNodes.length > 0) {
				menuConfiguration.children = [];

				Ext.Array.forEach(node.childNodes, function (childNode, i, allChildrenNodes) {
					childNode.set('parent', node[CMDBuild.core.constants.Proxy.ID]);

					var childConf = this.getMenuConfiguration(childNode);
					childConf.index = i;

					menuConfiguration.children.push(childConf);
				}, this);
			}

			return menuConfiguration;
		},

		onMenuGroupAbortButtonClick: function () {
			this.onMenuGroupMenuSelected();
		},

		/**
		 * @param {String} folderName
		 */
		onMenuGroupAddFolderButtonClick: function (folderName) {
			if (!Ext.isEmpty(folderName)) {
				this.menuTreePanel.getRootNode().appendChild({
					text: folderName,
					type: 'folder',
					folderType: 'folder',
					leaf: false
				});

				this.view.addFolderField.reset();
			}
		},

		onMenuGroupMenuSelected: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_NAME] = this.cmfg('selectedMenuNameGet');

			CMDBuild.core.proxy.Menu.readConfiguration({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.MENU];

					if (!Ext.isEmpty(decodedResponse)) {
						var menu = this.buildTreeStructure(decodedResponse);

						this.menuTreePanel.getRootNode().removeAll();

						if (!Ext.isEmpty(menu[CMDBuild.core.constants.Proxy.CHILDREN])) // If empty has no children field
							this.menuTreePanel.getRootNode().appendChild(menu[CMDBuild.core.constants.Proxy.CHILDREN]);
					}
				}
			});

			CMDBuild.core.proxy.Menu.readAvailableItems({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.MENU];

					if (!Ext.isEmpty(decodedResponse)) {
						var menu = this.buildTreeStructure(decodedResponse);

						this.availableItemsTreePanel.getRootNode().removeAll();
						this.availableItemsTreePanel.getRootNode().appendChild(menu[CMDBuild.core.constants.Proxy.CHILDREN]);

						this.menuTreePanel.getStore().sort([
							{ property: CMDBuild.core.constants.Proxy.INDEX, direction: 'ASC' },
							{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
						]);
					}
				}
			});
		},

		/**
		 * Disable selection of root node
		 *
		 * @param {CMDBuild.model.menu.TreeStore} record
		 *
		 * @returns {Boolean}
		 */
		onMenuGroupMenuTreeBeforeselect: function (record) {
			if (!Ext.isEmpty(record))
				return !record.isRoot();

			return true;
		},

		onMenuGroupMenuTreeSelectionchange: function () {
			this.removeItemButton.setDisabled(!this.menuTreePanel.getSelectionModel().hasSelection());
		},

		onMenuGroupRemoveItemButtonClick: function () {
			var selectedNode = this.menuTreePanel.getSelectionModel().getSelection()[0];

			if (!Ext.isEmpty(selectedNode) && !Ext.isEmpty(selectedNode.get(CMDBuild.core.constants.Proxy.TYPE)))
				this.removeTreeBranch(selectedNode);
		},

		onMenuGroupRemoveMenuButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onMenuGroupSaveButtonClick: function () {
			var menuTree = this.getMenuConfiguration(this.menuTreePanel.getRootNode());
			menuTree[CMDBuild.core.constants.Proxy.TYPE] = 'root';

			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_NAME] = this.cmfg('selectedMenuNameGet');
			params[CMDBuild.core.constants.Proxy.MENU] = Ext.encode(menuTree);

			CMDBuild.core.proxy.Menu.save({
				params: params,
				scope: this,
				callback: function (options, success, response) {
					this.onMenuGroupMenuSelected();

					// Customization of CMDBuild.view.common.field.translatable.Utils.commit
					Ext.Object.each(this.view.translatableAttributesConfigurationsBuffer, function (key, value, myself) {
						if (
							!Ext.isEmpty(value)
							&& !Ext.isEmpty(value[CMDBuild.core.constants.Proxy.TRANSLATIONS])
						) {
							value[CMDBuild.core.constants.Proxy.TRANSLATIONS] = Ext.encode(value[CMDBuild.core.constants.Proxy.TRANSLATIONS]);

							CMDBuild.core.proxy.localization.Localization.update({
								params: value,
								success: function (response, options, decodedResponse) {
									CMDBuild.core.Message.success();
								}
							});
						}
					}, this);
				}
			});
		},

		removeItem: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_NAME] = this.cmfg('selectedMenuNameGet');

			CMDBuild.core.proxy.Menu.remove({
				params: params,
				scope: this,
				callback: function (options, success, response) {
					this.onMenuGroupMenuSelected();
				}
			});
		},

		removeTreeBranch: function (node) {
			while (node.hasChildNodes())
				this.removeTreeBranch(node.childNodes[0]);

			var nodeType = node.get(CMDBuild.core.constants.Proxy.TYPE);

			if (nodeType.indexOf(CMDBuild.core.constants.Proxy.REPORT) >= 0)
				nodeType = 'report';

			var originalFolderOfTheLeaf = this.availableItemsTreePanel.getRootNode().findChild(CMDBuild.core.constants.Proxy.FOLDER_TYPE, nodeType);

			if (!Ext.isEmpty(originalFolderOfTheLeaf)) {
				originalFolderOfTheLeaf.expand();
				originalFolderOfTheLeaf.appendChild(node.getData());
			}

			// Remove the node before adding it to the original tree
			node.remove();
		}
	});

})();
