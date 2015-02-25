(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
		extend: 'Ext.tree.Panel',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants' // TODO
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
		 */
		delegate: undefined,

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
			var me = this;

			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1,

				listeners: {
//					beforeedit: function(editor, e, eOpts) {
//						me.delegate.cmOn('onBeforeEdit', {
//							fieldName: e.field,
//							rowData: e.record.data
//						});
//					}
				}
			});

			Ext.apply(this, {
				plugins: [this.gridEditorPlugin],
			});

			this.callParent(arguments);
		}

		// TODO
	});

})();
