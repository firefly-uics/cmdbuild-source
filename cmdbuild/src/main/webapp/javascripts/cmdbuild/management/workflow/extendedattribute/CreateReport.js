CMDBuild.Management.CreateReport = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {

	repType: '',
	repCode: '',
	formatCombo: {},
	attributeList: [],
	formFields: [],
	
	//create the form and request the report
	initialize: function(extAttrDef) {
		this.repType = extAttrDef.Type;
		this.repCode = extAttrDef.Code;
		
		var comboEnabled = true;
		if(extAttrDef.forceextension) {
			comboEnabled = false;
		}
		
		this.formatCombo = new Ext.form.ComboBox({
			fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.createreport.format_label,
			name : 'reportExtension',
			enabled : comboEnabled,
			editable : false,
			disableKeyFilter : true,
			forceSelection : true,
			emptyText : ' ',// this.translation.PleaseSelect,
			triggerAction : 'all',
			mode : 'local',
			store : new Ext.data.SimpleStore( {
				id : 0,
				fields : [ 'value', 'text' ],
				data : [ 
			        [ 'pdf', 'PDF' ],
					[ 'csv', 'CSV' ],
					[ 'odt', 'ODT' ],
					[ 'rtf', 'RTF' ]
				]
			}),
			grow: false,
			width: 70,
			valueField: 'value',
			displayField: 'text',
			hiddenName: 'reportExtension'
		});
		
		if (extAttrDef.forceextension) {
			this.formatCombo.setValue(extAttrDef.forceextension);
		}
		
		this.formPanel = new Ext.FormPanel({
            monitorValid: true,            
            autoScroll: true,           
            labelWidth: 200,
            frame: true,
            region: 'center',
            style: {'padding': '5px', background: CMDBuild.Constants.colors.blue.background},
            items: [this.formatCombo]
		});
		
		return {
			region: 'border',
			items: [this.formPanel]
		};
	},
	
	buildSpecificButtons: function() {
		this.saveButton = new Ext.Button({
            text : CMDBuild.Translation.common.buttons.save,
            name: 'saveButton',
            formBind : true,
            scope : this,
            handler : function() {
                this.submitParameters();
            }
        });		
		return [this.saveButton];
	},
	
	//add the required attributes
	configureForm: function() {
		if (!this.formPanelCreated) {
			this.formPanelCreated = true;
			var conf = this.extAttrDef;
			// add fields to form panel
	        for (var i=0; i<this.attributeList.length; i++) {
	            var attribute = this.attributeList[i];
	            var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);
	            if (field) {
	            	if(conf.parameters[attribute.name] && typeof (conf.parameters[attribute.name] != 'object')) {
	            		field.setValue( conf.parameters[attribute.name] );
	            	} else if(attribute.defaultvalue) {
	                    field.setValue(attribute.defaultvalue);
	                }
	                this.formFields[i] = field;
	                this.formPanel.add(field);
	            }
	        }
	        this.formPanel.doLayout();
		}
	},

	onExtAttrShow: function(extAttr) {
		this.setupReport();
	},

	fillFormValues: function() {
		var conf = this.extAttrDef;
		for(var i=0;i<this.formFields.length;i++) {
			var field = this.formFields[i];
			if(conf.parameters[field.name] && (typeof conf.parameters[field.name] == 'object')) {
				var value = this.getActivityFormVariable(conf.parameters[field.name]);
				field.setValue(value);
			}
		}
    },
    
    onSave: function() {
    	if (this.submittedFormValues) {
            this.react(this.submittedFormValues);
    	}
    },
	
	setupReport: function(callback) {
		Ext.Ajax.request({
            url: 'services/json/management/modreport/createreportfactorybytypecode',
            params: {
            	type: this.repType,
            	code: this.repCode
            },
            success: function(response) {
                var ret = Ext.util.JSON.decode(response.responseText);
                if(ret.filled) { // report with no parameters
                }
                else { // show form with launch parameters
                	this.attributeList = ret.attribute;
                }
                this.configureForm();
                this.fillFormValues();
            },
            scope: this
        });
	},
	
	submitParameters: function() {
		this.submittedFormValues = undefined;
        var form = this.formPanel.getForm();
        if (form.isValid()) {
        	CMDBuild.LoadMask.get().show();
            form.submit({
                method : 'POST',
                url : 'services/json/management/modreport/updatereportfactoryparams',                
                scope: this,
                success : function(form, action) {
            		this.submittedFormValues = form.getValues();
                    var popup = window.open("services/json/management/modreport/printreportfactory?donotdelete=true", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
                    if(!popup) {
                    	CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
                    }
                    this.backToActivityTab();
                    CMDBuild.LoadMask.get().hide();
                },
                failure: function() {
                	CMDBuild.LoadMask.get().hide();
                }
            });
        }
    }
});
Ext.reg('createReport', CMDBuild.Management.CreateReport);