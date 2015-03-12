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
			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
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

			CMDBuild.core.proxy.Localizations.getLanguagesToTranslate({
				// TODO costruzione delle colonne della griglia
			});

			Ext.apply(this, {
				plugins: [this.gridEditorPlugin],
				columns: [
					{
						xtype: 'treecolumn',
						text: '@@ Translation object',
						dataIndex: 'task',
						width: 300,
						locked: true, // TODO
						sortable: false,
						draggable: false
					},
					{
						text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ defaultTranslation',
						dataIndex: 'duration',
						width: 300,
						sortable: false,
						draggable: false,

						editor: { xtype: 'textfield' } // TODO forse non serve
					},
					{
						text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ langTag1',
						dataIndex: 'user',
						width: 300,
						sortable: false,
						draggable: false,

						editor: { xtype: 'textfield' }
					},
					{
						text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ langTag2',
						dataIndex: '@@ langTag2',
						width: 300,
						sortable: false,
						draggable: false,

						editor: { xtype: 'textfield' }
					}
				],
				store: CMDBuild.core.proxy.Localizations.getSectionTranslationsStore(/* TODO settare id sezione + lingue da includere */)
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Object} languageObject
		 * 		Ex. {
		 * 			{String} description: 'English',
		 * 			{String} tag: 'en'
		 * 		}
		 *
		 * @return {Mixed} returnObject
		 */
		buildColumn: function(languageObject) { // TODO
			var returnObject = null;

			if (Ext.isEmpty(languageObject)) {
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
			}

			return returnObject;
		}
	});

})();