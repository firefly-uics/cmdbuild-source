CMDBuild.Administration.ImportJRFormStep1 = Ext.extend(Ext.FormPanel, {
	translation: CMDBuild.Translation.administration.modreport.importJRFormStep1,
	id: 'reportJR1_panel',
	encoding: 'multipart/form-data', 
	fileUpload: true,
    labelWidth: 230,
    defaultType: 'textfield',
    monitorValid: true,
  	plugins: [new CMDBuild.CallbackPlugin()],
  	
  	initComponent:function() {
		this.fields = [{
			xtype: 'hidden',
			name: 'reportId',
			id: 'step1_reportId'
		},{
	        fieldLabel: this.translation.name,
	        allowBlank: false,
	        width : 258,
	        name: 'name',
	        id: 'step1_name',
	        disabled: true
		},{
	      	xtype : "textarea",
	      	width : 258,
	        fieldLabel: this.translation.description,
	        allowBlank: false,
	        name: 'description',
	        maxLength: 100,
	        disabled: true
		},{
			id: 'step1_multiselects_groups',
			xtype: "multiselect",
			fieldLabel: this.translation.enabled_groups,
			name:"groups",
			dataFields:['id', 'description'],
			valueField:'id',
			displayField:'description',
			store: new Ext.data.JsonStore({
				url: 'services/json/management/modreport/getgroups',
		        root: "rows",
		        fields : ['id', 'description'],
		        autoLoad: true
			}),
			width: 258,
			height: 120,
			allowBlank:true,
			disabled: true,
			style: {
				marginBottom: '10px'
			}
		},{
	      	inputType : "file",	      	
	        fieldLabel: this.translation.master_report_jrxml,
	        allowBlank: false,
	        name: 'jrxml',
	        disabled: true
		}]
		
		Ext.apply(this, {
			autoHeight: true,
			autoScroll: true,
			defaultType: 'textfield',
			items: this.fields
		});   
 
		CMDBuild.Administration.ImportJRFormStep1.superclass.initComponent.apply(this, arguments);		
	},
	
	analyzeJasperReport: function() {		
		Ext.getCmp('step1_reportId').setValue(this.getCurrentReportId());
		Ext.getCmp('step1_name').enable();
		CMDBuild.LoadMask.get().show();
		this.getForm().submit({
			method : 'POST',
			url : 'services/json/schema/modreport/analyzejasperreport',			
			scope: this,
			success : function(form, action) {				
				var ret = action.result;
				this.fireEvent('cmdb-importjasper-step2', ret); 	         	   
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	},
	
	getCurrentReportId: function() {
		return this.currentId;
	},
	
	setReportId: function(val) {
		this.currentId = val;
	},
	
	resetForm: function() {
		this.getForm().reset();
	},
	
	loadReport: function(eventParams) {
		var thisForm = this.getForm();
		var record = this.buildRecordForForm(eventParams.record.data);
		this.setReportId(record.data.id);
		thisForm.reset();
		thisForm.loadRecord(record);
		Ext.getCmp('step1_multiselects_groups').setValue(eventParams.record.data.groups);
	},
	
	enableFieldsForModify: function() {
		this.enableAllField();
		Ext.getCmp('step1_name').disable();
	},
	
	//private
	buildRecordForForm: function(r) {
		var recordTemplate = Ext.data.Record.create ([
   		    {name: 'name', mapping: 'name'},
   		    {name: 'description', mapping: 'description'},
   		    {name: 'id', mapping: 'id'}
   		]);
		
		return new recordTemplate({
			name: r.title,
			description: r.description,
			id: r.id
		});
	}
});