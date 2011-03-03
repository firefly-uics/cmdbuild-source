CMDBuild.Management.ModImportCSV = Ext.extend(CMDBuild.ModPanel, {
	id : 'importCSV',
	modtype: 'importCSV',	
	layout: 'fit',
	hideMode:  'offsets',
	frame: true,
	border: false,
	translation: CMDBuild.Translation.management.modutilities.csv,
	initComponent: function() {

		this.updateBtn = new CMDBuild.buttons.UpdateButton({			
			scope: this,	
			disabled: true,
			handler: this.onUpdate			
    	});

		this.confirmBtn = new CMDBuild.buttons.ConfirmButton({			
			scope: this,
			disabled: true,
			handler: this.onConfirm
    	});
		
		this.abortBtn = new CMDBuild.buttons.AbortButton({
			scope: this,
			disabled: true,
			handler: this.onCancel
    	});
		
    	this.uploadCsvBtn = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.upload,
			scope: this,
			handler: this.onUploadCsv			
		});
    	
    	this.classList = new Ext.form.ComboBox({
    		store: CMDBuild.Cache.getClassesAsStoreWithoutSuperclasses(addEmptyOption=false),
    		fieldLabel : this.translation.selectaclass,
    		width: 230,
			name : 'classid',
			hiddenName : 'idClass',
			valueField : 'id',
			displayField : 'description',
			triggerAction: 'all',
			mode: "local",
			allowBlank : false,
			editable: false
    	});
    	
    	this.uploadOption = new Ext.form.FormPanel({    		
    		region: 'north',    		
    		encoding: 'multipart/form-data', 
    		fileUpload:true,
    		method: 'POST',
    		url : 'services/json/management/importcsv/uploadcsv',
    		monitorValid: true,
    		id: 'formcsv',
			frame: true,				
			labelWidth: 200,	
    		split: true,
			items: [this.classList, {
					xtype: 'textfield',
					inputType : "file",					
					width: 230,
		    		fieldLabel: this.translation.csvfile,
		    		allowBlank: false,
		    		name: 'filecsv'    	
				}, new Ext.form.ComboBox({ 
					name: 'separator',
					width: 230,
					fieldLabel: this.translation.separator,
					valueField: 'value',
					displayField: 'value',
					hiddenName: 'separator',
					store: new Ext.data.SimpleStore({
						fields: ['value'],
						data : [[','],[';'],['|']]
					}),
					mode: 'local',
					triggerAction: 'all',
					editable: false,
					allowBlank: false
				})],				
				buttonAlign: 'left',
				buttons: [this.uploadCsvBtn]
    	});
    	
    	this.uploadOption.on('clientvalidation', function(form, valid){
    		this.uploadCsvBtn.setDisabled(!valid);
    	}, this);
    	
    	this.csv = new CMDBuild.Management.CSVGrid({
    		disabled: true,
    		region: 'center',
    		frame: false,
    		style: {border: '1px '+CMDBuild.Constants.colors.blue.border+' solid'}
    	});
    	
    	this.csv.on('afteredit', function(){
    		this.confirmBtn.disable();
    	}, this);
    	
    	Ext.apply(this, {
    		title: this.translation.title,	
    		items:[{
        		xtype: 'panel',
        		layout: 'border',
        		autoScroll: true,
        		frame: false,
        		items: [this.uploadOption, this.csv]
        	}],
    		buttonAlign: 'center',
			buttons: [this.updateBtn, this.confirmBtn,this.abortBtn]
    	});
    	CMDBuild.Management.ModImportCSV.superclass.initComponent.apply(this, arguments);
    },
        
    onUploadCsv: function() {
    	CMDBuild.LoadMask.get().show();
    	this.uploadOption.getForm().submit({
    		method: 'POST',
			scope: this,
			success: function() {
    			this.csv.searchField.setValue("");
    			this.abortBtn.enable();
	    		this.updateBtn.enable();	
	    		this.csv.enable();
	    		this.disableFields(true);
    			this.updateGrid();
    			CMDBuild.LoadMask.get().hide();
    		},
    		failure: function() {
    			CMDBuild.LoadMask.get().hide();
    		}
		});
    },
    
    onUpdate: function() {
    	var records = this.csv.getRecordToUpload();
    	if (records.length == 0) {
    		CMDBuild.Msg.warn(this.translation.warning, this.translation.noupdate);
    	}
    	CMDBuild.Ajax.request({
    		method: 'POST',
    		url : 'services/json/management/importcsv/updatecsvrecords',
    		params: { data: Ext.util.JSON.encode(records) },
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope: this,
			success: function(a,b,c) {    			
    			this.updateGrid();
    		}
		});
    },

    updateGrid: function() {
    	CMDBuild.Ajax.request({
    		method: 'POST',
    		url : 'services/json/management/importcsv/getcsvrecords',
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope: this,
			success: function(a,b,c) {    			
	    		var callback = this.csv.updateGrid.createDelegate(this.csv, [c], true);
	    		CMDBuild.Management.FieldManager.loadAttributes(this.classList.getValue(), callback);
	    		this.disableConfirmIfIncalid(c);
    		}
		});
    },
    
    disableConfirmIfIncalid: function(c) {
		var invalid = this.haveInvalidAttribute(c);
		if (invalid)
			this.confirmBtn.disable();
		else 
			this.confirmBtn.enable();
    },
    
    haveInvalidAttribute: function(c) {
    	var records = c.rows;
    	for (var i = 0, len = records.length; i<len; i++){
    		var invalids = records[i]["invalid_fields"];
			for (var i in invalids)
				return true;
    	}
    	return false;
    },
    
	onConfirm: function() {
    	CMDBuild.LoadMask.get().show();
    	CMDBuild.Ajax.request({
    		method: 'POST',
    		url : 'services/json/management/importcsv/storecsvrecords',
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			timeout: 600000,
			scope: this,
			success: function(a,b,c) {
    			CMDBuild.LoadMask.get().hide();
    			CMDBuild.Msg.info(this.translation.info, this.translation.importsuccess);
	    		this.onCancel();
    		},
    		failure: function(a,b,c) {
    			CMDBuild.LoadMask.get().hide();
    			CMDBuild.Msg.error(this.translation.error, this.translation.importfailure, true);
    		}
		});
    },
    
    onCancel: function() {
    	this.csv.clearRecords();
    	this.csv.disable();
		this.uploadOption.getForm().reset();
		this.disableFields(false);		
		this.updateBtn.disable();
		this.abortBtn.disable();
    },
    
    disableFields: function(toDisable) {
    	if (toDisable) {
    		this.uploadOption.stopMonitoring();
    	} else {
    		this.uploadOption.startMonitoring();
    	}
		this.uploadOption.getForm().findField('filecsv').setDisabled(toDisable);
		this.uploadOption.getForm().findField('separator').setDisabled(toDisable);
		this.classList.setDisabled(toDisable);
		this.uploadCsvBtn.setDisabled(toDisable);
	}
    
});