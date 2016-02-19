(function() {

	Ext.define('CMDBuild.view.administration.navigationTree.tree.TreePanel', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.navigationTree.TreeNode'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.Tree}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		autoScroll: true,
		border: false,
		cls: 'cmdb-border-bottom',
		collapsible: true,
		considerAsFieldToDisable: true,
		disableSelection: true,
		enableColumnHide: false,
		frame: false,
		header: false,
		hideCollapseTool: true,
		sortableColumns: false, // BUGGED in ExtJs 4.2, workaround setting sortable: false to columns

		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						xtype: 'treecolumn',
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.navigationTree,
						flex: 2,
						sortable: false,
						draggable: false
					},
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.FILTER,
						text: CMDBuild.Translation.cqlFilter,
						flex: 1,
						align: 'left',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						editor: { xtype: 'textfield' }
					})
				],
				store: Ext.create('Ext.data.TreeStore', {
					model: 'CMDBuild.model.navigationTree.TreeNode',

					root: {
						text: 'ROOT',
						checked: true,
						expanded: false,
						children: []
					}
				}),
				plugins: [
					this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			checkchange: function (node, checked, eOpts) {
				this.delegate.cmfg('onNavigationTreeTabTreeCheckChange', {
					node: node,
					checked: checked
				});
			},
			itemexpand: function (node, eOpts) {
				this.delegate.cmfg('onNavigationTreeTabTreeNodeExpand', node);
			}
		}
	});

})();
