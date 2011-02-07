CMDBuild.Administration.ModReport = Ext.extend(CMDBuild.ModPanel, {
	translation: CMDBuild.Translation.administration.modreport,
	modtype: 'report',
	activeTab: 0,
	id: 'report_panel',	
	colorsConst: CMDBuild.Constants.colors.gray,
	
	initComponent: function() {
		 this.addReport = new Ext.Action({
			 iconCls:'add',
			 text: this.translation.add,
			 scope: this,
			 handler: this.onAddReport
		 });
		 
		this.grid = new CMDBuild.Management.ReportListGrid({
			exportMode: true,
			colorsConst: this.colorsConst,
			split: true,
			region: 'center'
		});		 
		
		this.form = new CMDBuild.Administration.ReportForm({
			cmdbMod: this, 
			border: false,
			frame: false,
			split: true,
			style: {'border-top': '1px '+this.colorsConst.border+' solid'},
			region: 'south'	
		});
		 
		 Ext.apply(this, {
			 title: 'Report',			 
			 tbar:[this.addReport],
			 layout: 'border',
			 items: [this.grid, this.form],
			 style: {background: 'red'}
		 });
    	
    	CMDBuild.Administration.ModReport.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-select-report', this.selectReport, this);
    	this.subscribe('cmdb-reload-report',this.grid.loadReports, this.grid);
    	this.form.step2.on('cmdb-importjasper-importsuccess', this.onImportSuccess, this);
    	this.form.step2.on('cmdb-importjasper-importfailure', this.onImportFailure, this);
    	this.form.on('cmdb-abort-report', this.onAbort, this)
	},
	
	selectReport: function(p) {
		if (p) {
			this.addReport.enable();
			this.form.getJasperFormStep1();
			this.reportType = p.id;
			this.publish('cmdb-init-report', this.reportType);
		}
	},
	
	//private
	onImportSuccess: function() {
		var msg = this.translation.importJRFormStep2.success.description[this.form.status]
		CMDBuild.Msg.info(this.translation.importJRFormStep2.success.title, msg);
		this.grid.loadReports();
		this.form.step1.resetForm();
		this.form.step1.disableAllField();
		this.form.step1.un('clientvalidation', this.form.onClientValidation, this.form);
		this.form.invokeForButtons('disable');
		this.addReport.enable();
	},
	
	//private
	onImportFailure: function() {
		this.form.getJasperFormStep1();
	},
	
	//private
	onAddReport: function() {
		this.addReport.disable();
		this.form.newReport();
		this.grid.clearSelections();
	},
	
	//private
	onAbort: function() {
		this.addReport.enable();
		this.grid.clearSelections();
	}
});
