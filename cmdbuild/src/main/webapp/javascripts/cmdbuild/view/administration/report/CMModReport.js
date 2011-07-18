Ext.define("CMDBuild.view.administration.report.CMModReport", {
	extend: "Ext.panel.Panel",

	translation: CMDBuild.Translation.administration.modreport,
	cmName: 'report',
	activeTab: 0,
	
	initComponent: function() {

		this.addReportButton = new Ext.button.Button( {
			iconCls : 'add',
			text : this.translation.add
		});

		this.grid = new CMDBuild.view.common.report.CMReportGrid({
            title : 'Report',
			tbar : [ this.addReportButton ],
            border: true,
			exportMode: true,
			split: true,
			region: 'north',
			height: "30%"
		});

		this.form = new CMDBuild.view.administration.report.CMReportForm({
			region: 'center'
		});

		Ext.apply(this, {
			frame: false,
			border: false,
			layout : 'border',
			items : [ this.form, this.grid ]
		});

		this.callParent(arguments);
	},

	onReportTypeSelected: function(report) {
		if (report) {
			this.grid.onReportTypeSelected(report);
			this.form.onReportTypeSelected(report);
		}
	}

});
