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
		 * @cfg {String} // TODO tipologia di parametro reale
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
			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', { // TODO
				clicksToEdit: 1,

//				listeners: {
//					beforeedit: function(editor, e, eOpts) {
//						me.delegate.cmOn('onBeforeEdit', {
//							fieldName: e.field,
//							rowData: e.record.data
//						});
//					}
//				}
			});

			Ext.apply(this, {
				plugins: [this.gridEditorPlugin],
				columns: [
					{
						xtype: 'treecolumn',
						text: '@@ Translation object name',
						dataIndex: 'name',
						width: 300,
						locked: true, // TODO
						sortable: false,
						draggable: false
					},
					{
						text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ defaultTranslation',
						dataIndex: 'default',
						width: 300,
						sortable: false,
						draggable: false,

						editor: { xtype: 'textfield' } // TODO forse non serve
					},
					{
						text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ it',
						dataIndex: 'it',
						width: 300,
						sortable: false,
						draggable: false,

						editor: { xtype: 'textfield' }
					},
					{
						text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ pt_BR',
						dataIndex: 'pt_BR',
						width: 300,
						sortable: false,
						draggable: false,

						editor: { xtype: 'textfield' }
					}
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.model.Localizations.translation} languageObject
		 *
		 * @return {Mixed} returnObject
		 */
		buildColumn: function(languageObject) { // TODO
			var returnObject = null;

			if (Ext.isEmpty(languageObject))
				returnObject = Ext.create('Ext.grid.column.Column', {
					text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/'
						+ languageObject[CMDBuild.core.proxy.CMProxyConstants.TAG] + '.png" alt="'
						+ languageObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] + ' language icon" /> '
						+ languageObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
					dataIndex: languageObject[CMDBuild.core.proxy.CMProxyConstants.TAG],
					width: 300,
					sortable: false,
					draggable: false,

					editor: { xtype: 'textfield' }
				});

			return returnObject;
		}
	});

})();