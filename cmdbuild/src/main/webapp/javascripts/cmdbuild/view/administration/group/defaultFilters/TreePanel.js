(function() {

	Ext.define('CMDBuild.view.administration.group.defaultFilters.TreePanel', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.DefaultFilters',
			'CMDBuild.model.group.defaultFilters.TreeNode'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.DefaultFilters}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		autoScroll: true,
		border: false,
		cls: 'cmborderbottom',
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
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						text: CMDBuild.Translation.className,
						flex: 3,
						sortable: false,
						draggable: false
					},
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_FILTER,
						text: CMDBuild.Translation.filter,
						flex: 1,
						align: 'left',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						editor: {
							xtype: 'combo',
							displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
							valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
							editable: false,
							forceSelection: true,

							store: CMDBuild.core.proxy.group.DefaultFilters.getClassFiltersStore(),
							queryMode: 'local'
						}
					})
				],
				store: Ext.create('Ext.data.TreeStore', {
					model: 'CMDBuild.model.group.defaultFilters.TreeNode',
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
								return this.delegate.cmfg('onGroupDefaultFiltersTreeBeforeEdit', e);
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();