(function() {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.defaultFilters.TreePanel', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.DefaultFilters',
			'CMDBuild.model.userAndGroup.group.defaultFilters.TreeNode'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.DefaultFilters}
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
		enableColumnHide: false,
		frame: false,
		header: false,
		hideCollapseTool: true,
		rootVisible: false,
		sortableColumns: false, // BUGGED in ExtJs 4.2, workaround setting sortable: false to columns

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						xtype: 'treecolumn',
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.className,
						flex: 3,
						sortable: false,
						draggable: false
					},
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.DEFAULT_FILTER,
						text: CMDBuild.Translation.filter,
						flex: 1,
						align: 'left',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						editor: {
							xtype: 'combo',
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							valueField: CMDBuild.core.constants.Proxy.ID,
							editable: false,
							forceSelection: true,

							store: CMDBuild.core.proxy.userAndGroup.group.DefaultFilters.getClassFiltersStore(),
							queryMode: 'local'
						}
					})
				],
				store: Ext.create('Ext.data.TreeStore', {
					model: 'CMDBuild.model.userAndGroup.group.defaultFilters.TreeNode',
					root: {
						text: 'ROOT',
						expanded: true,
						children: []
					}
				}),
				plugins: [
					this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1,

						listeners: {
							scope: this,
							beforeedit: function(editor, e, eOpts) {
								return this.delegate.cmfg('onUserAndGroupGroupTabDefaultFiltersTreeBeforeEdit', e);
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();