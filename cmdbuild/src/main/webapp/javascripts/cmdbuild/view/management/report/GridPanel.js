(function() {

	Ext.define('CMDBuild.view.management.report.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Report'
		],

		border: false,
		frame: false,

		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.name,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.TITLE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
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
							{
								icon: 'images/icons/ico_pdf.png',
								tooltip: CMDBuild.Translation.pdf,
								scope: this,

								getClass: function(value, metadata, record, rowIndex, colIndex, store) {
									return 'cm-action-col-icon-spacer';
								},

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onReportGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.proxy.CMProxyConstants.PDF
									});
								}
							},
							{
								icon: 'images/icons/ico_odt.png',
								tooltip: CMDBuild.Translation.odt,
								scope: this,

								getClass: function(value, metadata, record, rowIndex, colIndex, store) {
									return 'cm-action-col-icon-spacer';
								},

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onReportGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.proxy.CMProxyConstants.ODT
									});
								}
							},
							{
								icon: 'images/icons/ico_rtf.png',
								tooltip: CMDBuild.Translation.rtf,
								scope: this,

								getClass: function(value, metadata, record, rowIndex, colIndex, store) {
									return 'cm-action-col-icon-spacer';
								},

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onReportGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.proxy.CMProxyConstants.RTF
									});
								}
							},
							{
								icon: 'images/icons/ico_csv.png',
								tooltip: CMDBuild.Translation.csv,
								scope: this,

								getClass: function(value, metadata, record, rowIndex, colIndex, store) {
									return 'cm-action-col-icon-spacer';
								},

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onReportGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.proxy.CMProxyConstants.CSV
									});
								}
							}
						]
					})
				],
				store: CMDBuild.core.proxy.Report.getStore(),
			});

			this.callParent(arguments);
		}
	});

})();