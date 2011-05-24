(function() {

	var directRequest = function(grid, report, extension) {
		grid.bringReportPanelToFront(report);
		grid.requestReport({
			id: report.objid,
			type: report.subtype.toUpperCase(),
			extension: extension
		});
	};
	
	
/**
 * This is the Grid Panel that contains the report list of the selected report type
 * 
 * @class CMDBuild.Management.ReportListGrid
 * @extends CMDBuild.Grid
 */
Ext.define("CMDBuild.Management.ReportListGrid", {
	extend: "CMDBuild.Grid",
	alias: "reportlistgrid",

	translation : CMDBuild.Translation.management.modreport.reportForm,
	baseUrl : 'services/json/management/modreport/getreportsbytype',
	filtering : false,
	reportType : '',
	exportMode: false,
	autoScroll: true,
	
	initComponent : function() {
		CMDBuild.Management.ReportListGrid.superclass.initComponent.apply(this, arguments);			
		
		if (this.exportMode) {
			this.getSelectionModel().on('rowselect', this.reportSelected , this);
		} else {
			this.getSelectionModel().lock();
		}
		function cellclickHandler(grid, rowIndex, colIndex, event) {
			var reportExtension = event.target.className;
			var selectedRow = grid.getStore().getAt(rowIndex).json;
			if (reportExtension == 'pdf' || reportExtension == 'csv' || reportExtension == 'odt' || reportExtension == 'zip' || reportExtension == 'rtf') {
				this.requestReport({
						id: selectedRow.id,
						type: selectedRow.type,
						extension: reportExtension
					});
			} else if (reportExtension == 'sql') {
				var closeWin = function() {
					win.destroy();
				};
				
				var win = new CMDBuild.PopupWindow({
					title: 'Sql',
					items: [{
						xtype: 'panel',
						autoScroll: true,
						html: '<pre style="padding:5px; font-size: 1.2em">'+selectedRow.query+'</pre>'
					}],
					buttonAlign: 'center',
					buttons: [{
						text: CMDBuild.Translation.common.btns.close,						
						handler: closeWin
					}]
				}).show();
			}
		}
		this.on('cellclick', cellclickHandler);

		this.subscribe('cmdb-init-report', this.initForClass, this);
		this.subscribe('cmdb-reload-report', this.loadReports, this);
		
		this.subscribe('cmdb-select-reportpdf', this.reportDirectRequestPdf, this);
		this.subscribe('cmdb-select-reportcsv', this.reportDirectRequestCsv, this);
		this.subscribe('cmdb-select-reportodt', this.reportDirectRequestOdt, this);
		this.subscribe('cmdb-select-reportrtf', this.reportDirectRequestRtf, this);
		this.subscribe('cmdb-select-reportzip', this.reportDirectRequestZip, this);
	},

	initForClass : function(reportType) {
		
		this.reportType = reportType == "Jasper" ? "custom" : reportType;
		var scope = this;
		function loadReportIcons(reportType,x,store) {
			if(reportType=='CUSTOM') {
				var html ="";
				if (scope.exportMode) {
					html += '<img qtip="Sql" style="cursor:pointer" class="sql" src="images/icons/ico_sql.png"/>&nbsp;&nbsp;';
					html += '<img qtip="Zip" style="cursor:pointer" class="zip" src="images/icons/ico_zip.png"/>&nbsp;&nbsp;';
				} else {
					html += '<img qtip="Adobe Pdf" style="cursor:pointer" class="pdf" src="images/icons/ico_pdf.png"/>&nbsp;&nbsp;';
					html += '<img qtip="OpenOffice Odt" style="cursor:pointer" class="odt" src="images/icons/ico_odt.png"/>&nbsp;&nbsp;';
					html += '<img qtip="Rich Text Format" style="cursor:pointer" class="rtf" src="images/icons/ico_rtf.png"/>&nbsp;&nbsp;';
					html += '<img qtip="Csv" style="cursor:pointer" class="csv" src="images/icons/ico_csv.png"/>&nbsp;&nbsp;';
				};
				return html;
			} else {
				//openoffice
			}
		}
		
		var headers = [{
			header : "Id",
			dataIndex : "id",
			hidden:true
		},{
			header : "Query",
			dataIndex : "query",
			hidden:true
		},{
			header : this.translation.name,
			sortable : true,
			dataIndex : "title"
		},{
			header : this.translation.description,
			sortable : true,
			dataIndex : "description"
		},{
			header : this.translation.report,
			sortable : false,
			dataIndex : "type",
			width: this.exportMode ? 50 : 100,
			fixed: true,
			renderer: loadReportIcons,
			menuDisabled: true,
			id: 'imagecolumn',
			hideable: false
		}];		

		this.setColumns(headers);
		this.loadReports();
	},

	loadReports : function() {
		var store = this.getStore();
		store.baseParams = {
				type : this.reportType
			};
		store.load({
			params : {
				start: 0,
				limit: 20
			}
		});
		this.clearSelections();		
	},

	reportDirectRequestPdf: function(report) {
		directRequest(this, report, "pdf");
	},

	reportDirectRequestCsv: function(report) {
		directRequest(this, report, "csv");		
	},
	
	reportDirectRequestOdt: function(report) {
		directRequest(this, report, "odt");
	},
	
	reportDirectRequestZip: function(report) {
		directRequest(this, report, "zip");
	},

	bringReportPanelToFront: function(report) {
		this.publish('cmdb-select-report', { id: report.subtype.toUpperCase() });
	},

	requestReport: function(reportParams, maskedElement) {
		this.getEl().mask(CMDBuild.Translation.common.wait_title,'x-mask-loading');
		Ext.Ajax.request({
			url: 'services/json/management/modreport/createreportfactory',
			params: reportParams,
			success: function(response) {
				var ret = Ext.util.JSON.decode(response.responseText);
				if(ret.filled) { // report with no parameters
					var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
					if (!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
				}
				else { // show form with launch parameters
					var paramWin = new CMDBuild.Management.ReportParamWin({
						attributeList: ret.attribute
					});
					paramWin.show();
				}
			},
			callback : function() {
				this.getEl().unmask();
	      	},
	      	scope: this
		});
	},

    reportSelected: function(sm, row, rec) {
		var eventParams = {
			record: new Ext.data.Record(rec.json)
		}
		this.publish('cmdb-load-report', eventParams);
		CMDBuild.log.info('cmdb-load-report', eventParams);
	},
	
	clearSelections: function() {
		this.getSelectionModel().clearSelections();
	}
});
})();