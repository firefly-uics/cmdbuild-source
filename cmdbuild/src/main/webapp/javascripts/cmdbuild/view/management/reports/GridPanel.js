(function() {

	Ext.define('CMDBuild.view.management.reports.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.reports.Reports'
		],

		/**
		 * @cfg {CMDBuild.controller.management.reports.Reports}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			var store = CMDBuild.core.proxy.reports.Reports.getStore();

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Paging', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
						store: store,
						displayInfo: true,
						displayMsg: '{0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
						emptyMsg: CMDBuild.Translation.common.display_topic_none
					})
				],
				columns: [
					{
						text: CMDBuild.Translation.name,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.Constants.TITLE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.Constants.DESCRIPTION,
						flex: 1
					},
					Ext.create('Ext.grid.column.Action', {
						text: CMDBuild.Translation.report,
						align: 'center',
						width: 120,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.FileFormatsPdf', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.pdf,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportGenerateButtonClick', {
										record: record,
										extension: CMDBuild.core.proxy.Constants.PDF
									});
								}
							}),
							Ext.create('CMDBuild.core.buttons.FileFormatsOdt', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.odt,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportGenerateButtonClick', {
										record: record,
										extension: CMDBuild.core.proxy.Constants.ODT
									});
								}
							}),
							Ext.create('CMDBuild.core.buttons.FileFormatsRtf', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.rtf,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportGenerateButtonClick', {
										record: record,
										extension: CMDBuild.core.proxy.Constants.RTF
									});
								}
							}),
							Ext.create('CMDBuild.core.buttons.FileFormatsCsv', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.csv,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportGenerateButtonClick', {
										record: record,
										extension: CMDBuild.core.proxy.Constants.CSV
									});
								}
							})
						]
					})
				],
				store: store
			});

			this.callParent(arguments);
		}
	});

})();