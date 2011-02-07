CMDBuild.Administration.ImportJRFormStep2 = Ext.extend(Ext.FormPanel, {
	translation: CMDBuild.Translation.administration.modreport.importJRFormStep2,
	id: 'reportJR2_panel',
	encoding: 'multipart/form-data', 
	fileUpload: true,
  	labelWidth: 230,
    defaultType: 'textfield',   
    monitorValid: true,
    plugins: [new CMDBuild.CallbackPlugin()],
    
    initComponent:function() {
		this.fieldsets = [];		
		
		Ext.apply(this, {
			frame: true,
			layout: 'fit'			
		});
		
		CMDBuild.Administration.ImportJRFormStep2.superclass.initComponent.apply(this, arguments);
		this.on('show', this.initStep, this);
	},
	
	setFormDetails: function(fd) {
		this.duplicateimages = false;//because it was overridden only if is true
		Ext.apply(this, fd);
	},
		
	insertJasperReport: function() {
		CMDBuild.LoadMask.get().show();
		this.getForm().submit({
			method : 'POST',
			url : 'services/json/schema/modreport/importjasperreport',			
			scope: this,
			success : function(form, action) {
				this.resetSession();
				this.fireEvent('cmdb-importjasper-importsuccess');
			},
			failure: function() {
				this.fireEvent('cmdb-importjasper-importfailure');
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	},
	
	resetSession: function() {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/modreport/resetsession',
			method: 'POST',
			params: {}, 			
			scope: this
		});
		this.fireEvent('cmdb-importjasper-step1', null);
	},
	
	//private
	initStep: function() {		
		this.fieldsets = [];
		this.removeAll(true);
		
  		if(this.duplicateimages) {
  			this.printMsg(this.translation.duplicate_images);
  			this.fireEvent('cmdb-importjasper-duplicateimages');
  		} else { //show form
  			var imagesFields = this.buildFieldArray(this.images, "image");
		  	var subreportFields = this.buildFieldArray(this.subreports, "subreport");
		  	
		  	this.addToFildsetsIfNotEmpty(imagesFields);
		  	this.addToFildsetsIfNotEmpty(subreportFields);
		  	
		  	// if no field(image or subreport), skip step2 and insert the report
		  	if(this.fieldsets.length==0) {
		  		this.insertJasperReport();
		  	}
		}
  		this.addFieldsetToContainer();
	},
	
	/*
	 * this function creates an array and fill it with one field, with
	 * inputType = "file", for each item in the refer array
	 *  
	 */
	
	//private
	addFieldsetToContainer: function() {
		var container = new Ext.form.FieldSet({
			title: this.translation.fieldset,
			autoHeight: true,
			autoScroll: true,
			defaultType: 'textfield',
			items: [this.fieldsets]
		});
		this.add(container);	
		this.doLayout();
	},
	
	//private
	buildFieldArray: function(refer, namePrefix) {
		var out = [];
		if (refer) {
			for (var i=0; i < refer.length; i++) {
				var image = refer[i];
				if (image.name) {
					var field = new Ext.form.TextField({
						allowBlank: false,
						inputType : "file",
						fieldLabel: image.name,
						name: namePrefix+i
					});
					out.push(field);
					this.getForm().add(field);
				} else {
					CMDBuild.log.error('Import report step 2: ', image, 'has not an attribute called name');
				} 
		  	}
		}
		return out;
	},
	
	//private
	addToFildsetsIfNotEmpty: function(arr) {
		if(arr.length>0) {
			var fs = new Ext.form.FieldSet({
				title: this.translation.fieldset_subreports,
			    autoHeight: true,
			    labelWidth: 230,
			    items: arr
			});
	  		this.fieldsets.push(fs);
	  	}
	},
	
	//private
	printMsg: function(msg) {
		var msg = new Ext.form.FieldSet({
			title: this.translation.fieldset_generic,
			autoHeight: true,
			items: [{
				xtype : "label",
				text : msg
			}]
		});
		this.fieldsets.push(msg);
	}
});