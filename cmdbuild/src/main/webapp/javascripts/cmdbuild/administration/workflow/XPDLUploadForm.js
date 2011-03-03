CMDBuild.Administration.XPDLUpForm = Ext.extend(Ext.form.FormPanel, {
	translation: CMDBuild.Translation.administration.modWorkflow.xpdlUpload,

	initComponent: function() {
		this.id = 'xpdluploadform';
		this.classId = -1;
		
		this.initialConfig.fileUpload = true;
		
		this.submitButton = new Ext.Button({
            text: this.translation.upload_template,
            id: 'uploadxpdltemplatebutton',
            name: 'uploadxpdltemplatebutton',
            formBind : true,
            scope: this,
            handler: function() {
				CMDBuild.LoadMask.get().show();
                this.getForm().submit({
                    url: 'services/json/schema/modworkflow/uploadxpdl',                   
                    scope: this,
                    success: function(form, action) {
                		CMDBuild.LoadMask.get().hide();
                		var result = Ext.decode(action.response.responseText);
                		var msg = "<ul>";
                		for (var i=0, len=result.messages.length; i<len; ++i) {
                			msg += "<li>"+this.translation[result.messages[i]]+"</li>";
                		}
                		msg+="</ul>";
                    	CMDBuild.Msg.info(
                    		CMDBuild.Translation.common.success, msg);
                    },
                    failure: function() {
                    	CMDBuild.LoadMask.get().hide();
                    	CMDBuild.Msg.error(
                    		CMDBuild.Translation.common.failure,
                    		this.translation.error, true);
                    }
                });
            }
        });
		
		Ext.apply(this, {
            title : this.translation.upload_xpdl_tamplete,
            labelWidth : 200,
            defaultType : 'textfield',
            monitorValid : true,
            layout: 'fit',           
            items: [{
            	xtype: 'panel',
            	layout: 'form',
            	frame: true,
            	border: true,
            	items : [{
	                name : 'idClass',
	                xtype : 'hidden',
	                value : -1
	            },{
	                name: 'userstoppable',
	                fieldLabel: CMDBuild.Translation.administration.modWorkflow.xpdlDownload.user_stoppable,
	                xtype: 'xcheckbox'
	            },{
	            	xtype: 'textfield',
					inputType : 'file',
					allowBlank: true,
					width: 300,            	
	            	inputType: 'file',
	                name: 'xpdlfile',
	                fieldLabel: this.translation.xpdl_file
	            },{
	            	xtype: 'textfield',
	                inputType: 'file',
	                name: 'imgfile',
	                allowBlank: true,
					width: 300,
	                fieldLabel: this.translation.jpg_file
	            }]
            }],
            buttonAlign: 'center',
            buttons: [this.submitButton]
        });
        
        CMDBuild.Administration.XPDLUpForm.superclass.initComponent.apply(this, arguments);
        this.subscribe('cmdb-xpdl-loaded',this.loadData, this);
        this.subscribe('cmdb-init-processclass', function(){
        	this.getForm().reset();
        }, this);
	},
	
	loadData: function(params) {
        
        /*
         * if the form has not been rendered yet, async call loadData on afterlayout
         */
        if(!this.rendered) {
            this.on('afterlayout',function(){this.loadData(params);},this,{single: true});
            return;
        }
                
        this.idClass = undefined;
        if(params.idClass) {
            this.idClass = params.idClass;
        }
        
        if(this.idClass) {
        	this.getForm().setValues(params);
        } else {
        	this.getForm().reset();
        }
    }
});

Ext.reg('xpdluploadform', CMDBuild.Administration.XPDLUpForm);