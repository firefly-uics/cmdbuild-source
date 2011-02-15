CMDBuild.Management.ModChangePassword = Ext.extend(CMDBuild.ModPanel, {
	id : 'changepassword',
	modtype: 'changepassword',
	title : CMDBuild.Translation.management.modutilities.changepassword.title,
	layout: 'fit',
	frame: false,
	border: true,	
	initComponent: function() {
		//vtyepe for passwords validation
		Ext.apply(Ext.form.VTypes, {
		    password : function(val, field) {
		        if (field.initialPassField) {
		            var pwd = Ext.getCmp(field.initialPassField);
		            return (val == pwd.getValue());
		        }
		        return true;
		    },
		    passwordText : CMDBuild.Translation.management.modutilities.changepassword.validationtext
		});
		//save button
		this.saveBtn = new Ext.Button({
			id:'confirmButton',
			text: CMDBuild.Translation.common.buttons.save,
			scope: this,
			handler: function(){
				var values = this.form.getForm().getValues();
				CMDBuild.log.info(values);
				
				this.form.getForm().submit({
					scope: this,
					callback: function(){this.form.getForm().reset()},
					success: function(){
						CMDBuild.Msg.info("", CMDBuild.Translation.management.modutilities.changepassword.successmsgtext);
					}
				});

			}
    	});
    		
    	this.abortBtn = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.abort,
			scope: this,
			handler: function(){
				this.form.getForm().reset();
			}
		});
		
		this.form = new Ext.form.FormPanel({
			labelWidth: 200,
			monitorValid: true,
			plugins: new CMDBuild.CallbackPlugin(),
			url: 'services/json/schema/modsecurity/changepassword',
			frame: true,
			border: false,
			items: [{
				xtype: 'textfield',
				inputType:'password', 
				fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.oldpw,
				name: 'oldpassword',
				allowBlank: false
			},{
				xtype: 'textfield',
				inputType:'password', 
				fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.newpw,
				name: 'newpassword',
				id:'newpassword',
				allowBlank: false
			},{
				xtype: 'textfield',
				inputType:'password', 
				fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.repnew,
				name: 'confirmnewpassword',
				vtype: 'password',
				initialPassField: 'newpassword',
				allowBlank: false
			}]
		});
    	Ext.apply(this, {
    		frame: true,
    		border: true,		
    		items:[this.form],
    		buttonAlign: 'center',
			buttons: [this.saveBtn, this.abortBtn],
			disabled: !CMDBuild.Runtime.CanChangePassword
    	});
    	CMDBuild.Management.ModChangePassword.superclass.initComponent.apply(this, arguments);
    	
    	this.form.on('clientvalidation', function(panel, valid){
    		this.saveBtn.setDisabled(!valid);
    	}, this)
    }
});
