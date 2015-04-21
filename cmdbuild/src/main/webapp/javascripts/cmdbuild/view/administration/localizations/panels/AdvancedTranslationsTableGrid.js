(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: undefined,

		autoScroll: true,
		border: false,
		collapsible: true,
		columnLines: true,
		enableColumnHide: false,
		frame: false,
		header: false,
		hideCollapseTool: true,
		rootVisible: false,
		sortableColumns: false, // BUGGED in ExtJs 4.2, workaround setting sortable: false to columns

		initComponent: function() {
			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.RowEditing', {
				clicksToEdit: 2,
				autoCancel: false,

				listeners: {
					// Permits to edit only leaf nodes
					beforeedit: function(editor, context, eOpts) {
						if (!context.record.isLeaf())
							return false;
					}
				}
			});

			Ext.apply(this, {
				plugins: [this.gridEditorPlugin]
			});

			this.callParent(arguments);
		},

		listeners: {
			beforeitemexpand: function(node, eOpts) {
				this.delegate.cmOn('onAdvancedTableNodeExpand', node);
			},
			edit: function(editor, context, eOpts) {
				this.delegate.cmOn('onAdvancedTableRowUpdateButtonClick', context.record);
			}
		}
	});

})();