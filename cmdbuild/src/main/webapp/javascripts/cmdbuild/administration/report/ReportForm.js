CMDBuild.Administration.ReportForm = Ext.extend(Ext.Panel, {
	translation: CMDBuild.Translation.administration.modreport.importJRFormStep1,
	colorsConst: CMDBuild.Constants.colors.gray,
	initComponent:function() {
	
		this.modifyAction = new Ext.Action({	
	    	iconCls : 'modify',
	    	text : CMDBuild.Translation.administration.modreport.modify,
	    	handler : this.onModify,
	    	scope : this,
	    	disabled: true
	    });
    
	   	this.deleteAction = new Ext.Action({	
	    	iconCls : 'delete',
	    	text : CMDBuild.Translation.administration.modreport.remove,
	    	handler : this.onDelete,
	    	scope : this, 	
	    	disabled: true
	    });

	    this.saveButton = new Ext.Button({
	        text : CMDBuild.Translation.common.buttons.save,
	        scope : this,
	        handler : this.onSave,
	        disabled : true
	    });
    
	    this.abortButton= new Ext.Button({
	    	text : CMDBuild.Translation.common.buttons.abort,
	    	scope : this,
	    	handler : this.onAbort,
	    	disabled : true
	    });
	    
	    this.step1 = new CMDBuild.Administration.ImportJRFormStep1({
	    	frame: true,
	    	border: true,
			style: {padding: '5px', background: this.colorsConst.background}
	    });
	    this.step2 = new CMDBuild.Administration.ImportJRFormStep2({
	    	frame: true,
	    	border: true,
			style: {padding: '5px', background: this.colorsConst.background}
	    }).hide();
	    
	    Ext.apply(this, {
			tbar : [this.modifyAction, this.deleteAction],
	  	  	items: [this.step1, this.step2],
			buttonAlign: 'center',
			autoScroll: true,
	        buttons : [this.saveButton, this.abortButton]
	    });
	    
		CMDBuild.Administration.ReportForm.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-load-report', this.onLoadReport, this);
		this.step1.on('cmdb-importjasper-step2', this.getJasperFormStep2, this);		
		this.step2.on('cmdb-importjasper-step1', this.getJasperFormStep1, this);
		this.step2.on('cmdb-importjasper-duplicateimages', function() {
			this.step2.un('clientvalidation', this.onClientValidation, this);
			this.saveButton.hide();
			CMDBuild.log.info('duplicateimg')
		}, this);
	},
	
	newReport: function() {
		this.status = "add";
		this.step1.onNewReport();
		this.step1.on('clientvalidation', this.onClientValidation, this);
		this.invokeForButtons('enable');
		this.invokeForTbar('disable');
	},
	
	//private
	onClientValidation: function(form, valid) {
		this.saveButton.setDisabled(!valid);
	},
	
	//private
	onLoadReport: function(p) {
		this.invokeForTbar('enable');
		this.step1.loadReport(p);
		this.step1.disableAllField();		
	},
	
	//private
	onModify: function() {		
		this.status = "modify";
		this.step1.on('clientvalidation', this.onClientValidation, this);
		this.invokeForButtons('enable');
		this.invokeForTbar('disable');
		this.step1.enableFieldsForModify();
		this.cmdbMod.addReport.disable();
	},
	
	//private
	onSave: function() {
		if (this.step1.isVisible()) {
			this.step1.analyzeJasperReport();
		} else {
			this.step2.insertJasperReport();
		}
	},
	
	//private
	onDelete: function() {
		Ext.Msg.show({
			title: CMDBuild.Translation.administration.modreport.remove,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: {
				yes: true,
				no: true
			},
			fn: function(button){
				if (button == 'yes'){
					this.deleteReport();
				}
			}	
		});
		
	},	
	
	//private
	deleteReport: function() {
		CMDBuild.LoadMask.get().show(); 
		CMDBuild.Ajax.request({
			url : 'services/json/management/modreport/deletereport',
			params : {				
				"id": this.step1.getCurrentReportId()
			},			
			method : 'POST',
			scope : this,
			success : function(response) {
				this.getJasperFormStep1();
				this.step1.resetForm();
				this.publish('cmdb-reload-report');
				this.invokeForButtons('disable');
				this.invokeForTbar('disable');
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide(); 
			}
  	 	});
	},
	
	//private
	onAbort: function() {
		this.step1.un('clientvalidation', this.onClientValidation, this);
		this.step2.resetSession();
		this.step1.resetForm();
		this.step1.disableAllField();
		this.invokeForButtons('disable');
		this.invokeForTbar('disable');
		this.fireEvent('cmdb-abort-report');
		this.saveButton.show();
	},
	
	getJasperFormStep1: function() {
		this.step2.hide();
		this.step2.un('clientvalidation', this.onClientValidation, this);
		this.step1.show();
	},

	getJasperFormStep2: function(formDetails) {
		this.step1.hide();
		this.step1.un('clientvalidation', this.onClientValidation, this);
		this.step2.setFormDetails(formDetails);
		this.step2.show();
		this.step2.on('clientvalidation', this.onClientValidation, this);
	},
		
	invokeForButtons: function(fnName) {
		for (var i=0, arr = this.buttons ; i<arr.length; i++) {
			arr[i][fnName]()
		};
	},
	
	invokeForTbar: function(fnName) {
		for (var i=0, arr = this.topToolbar.items.items ; i<arr.length; i++) {
			arr[i][fnName]()
		};
	}
});

Ext.reg('adminreportform', CMDBuild.Administration.ReportForm );