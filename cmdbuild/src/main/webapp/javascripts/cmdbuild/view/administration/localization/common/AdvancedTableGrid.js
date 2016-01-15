(function() {

	Ext.define('CMDBuild.view.administration.localization.common.AdvancedTableGrid', {
		extend: 'Ext.tree.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.RowEditing}
		 */
		gridEditorPlugin: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: undefined,

		autoScroll: true,
		border: false,
		collapsible: true,
		columnLines: true,
		frame: false,
		header: false,
		hideCollapseTool: true,
		rootVisible: false,
		sortableColumns: false, // BUGGED in ExtJs 4.2, workaround setting sortable: false to columns

		initComponent: function() {
			Ext.apply(this, {
				plugins: [
					this.gridEditorPlugin = Ext.create('Ext.grid.plugin.RowEditing', {
						clicksToEdit: 2,
						autoCancel: false,

						listeners: {
							/**
							 * Permits to edit only leaf nodes
							 */
							beforeedit: function(editor, context, eOpts) {
								if (!context.record.isLeaf())
									return false;
							}
						}
					}),
					Ext.create('Ext.grid.plugin.BufferedRenderer')
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			edit: function(editor, context, eOpts) {
				this.delegate.cmfg('onLocalizationAdvancedTableRowUpdateButtonClick', context.record);
			}
		}
	});

})();