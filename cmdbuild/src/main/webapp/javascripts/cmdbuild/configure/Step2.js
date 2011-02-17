Ext.ns("CMDBuild.Configure");

CMDBuild.Configure.Step2 = Ext.extend( Ext.form.FormPanel, {
	plugins: new CMDBuild.CallbackPlugin(),	
	initComponent: function() {
		var tr = CMDBuild.Translation.configure.step2;	
		var cardNav = function(incr){
			var l = Ext.getCmp('configure_wizard_panel').getLayout();
			var i = l.activeItem.id.split('card-')[1];
			var next = parseInt(i) - 1 + incr;
			l.setActiveItem(next);
		};
		
		//vtype for passwords validation
		Ext.apply(Ext.form.VTypes, {
		    password : function(val, field) {
		        if (field.initialPassField) {
		            var pwd = Ext.getCmp(field.initialPassField);
		            return (val == pwd.getValue());
		        }
		        return true;
		    },
		    passwordText : CMDBuild.Translation.configure.step2.msg.pswnomatch
		});
		
		this.nextStep = new Ext.Button({
			id: 'card-next-step2',
			iconCls: 'arrow_right',
			handler: cardNav.createDelegate(this, [1]),
			text: CMDBuild.Translation.configure.next,
			disabled: true
		});
		
		this.finishBtn = new Ext.Button({
			id: 'finish',
			iconCls: 'add',
			handler: this.applyConfiguration,
			text: CMDBuild.Translation.configure.finish,
			hidden: true,
			disabled: true
		});
		
		this.connectionButton = new Ext.Button({
			text: tr.db_test_connection,
			id:'testConnection_button',				
			scope: this,
			handler: this.testConnection,
			disabled: true
		});
		
		this.dbType = new Ext.form.ComboBox( { 
			name: 'db_type',
			fieldLabel: tr.db_create_type,
			valueField: 'name',
			displayField: 'value',
			hiddenName: 'db_type',
			store: new Ext.data.SimpleStore({
				fields: ['name', 'value'],
				data : [
				        ['empty', tr.db_empty],
				        ['demo', tr.db_demo],
				        ['existing', tr.db_existing]
				]
			}),
			mode: 'local',
			triggerAction: 'all',
			editable: false,
			value: 'empty'
		});
		
		this.sharkSchema = new Ext.ux.form.XCheckbox({
			name: 'shark_schema',
			fieldLabel: tr.create_shark
		});
		
		this.dbTypeAndNameFieldSet = new Ext.form.FieldSet({
			title: tr.db_create,
            autoHeight:true,
            defaultType: 'textfield',
			items: [
				this.dbType,
				{
					name: 'db_name',
					fieldLabel: tr.db_create_name,
					allowBlank: false
				},
				this.sharkSchema]
		});
		
		this.validationFieldListeners = {
			'valid' : {
		        fn: function(field){
					this.checkDbconnectionValidation();
				},
		        scope: this				        
		    }, 
		    'invalid' : {
		        fn: function(field){
		    		this.connectionButton.disable();
			},
	        scope: this				        
	    }
		};
		
		this.host = new Ext.form.TextField({
			name: 'host',
			allowBlank: false,
			fieldLabel: tr.db_host,
			listeners: this.validationFieldListeners
		});
		
		this.port = new Ext.form.TextField({
			name: 'port',
			allowBlank: false,
			fieldLabel: tr.db_port,
			listeners: this.validationFieldListeners
		});
		
		this.user = new Ext.form.TextField({
			name: 'user',
			allowBlank: false,
			fieldLabel: tr.db_superUser,
			listeners: this.validationFieldListeners
		});
		
		this.password = new Ext.form.TextField({
			name: 'password',
			id: 'password',
			allowBlank: false,
			inputType:'password', 
			fieldLabel: tr.db_password,
			listeners: this.validationFieldListeners
		});
		
		this.dbConnectionFieldset = new Ext.form.FieldSet({
			title: tr.db_connection + ' (PostgreSQL ' + 
				CMDBuild.Config.cmdbuild.jdbcDriverVersion +')',
            autoHeight:true,
            
            collapsed: false,
            buttonAlign: 'left',
			items: [
			        this.host,
			        this.port,
			        this.user,
			        this.password
			],
			buttons: [this.connectionButton]			
		});	
		
		this.userType = new Ext.form.ComboBox( { 
			name: 'user_type',
			fieldLabel: tr.user_type,
			valueField: 'name',
			displayField: 'value',
			hiddenName: 'user_type',
			store: new Ext.data.SimpleStore({
				fields: ['name', 'value'],
				data : [
				        ['superuser', tr.db_superUser],
				        ['limuser', tr.limited_user],
				        ['new_limuser', tr.new_limited_user]
				]
			}),
			mode: 'local',
			triggerAction: 'all',
			editable: false,
			value: 'superuser'
		});
		
		this.limUser = new Ext.form.TextField({
			name: 'lim_user',
			fieldLabel: tr.db_user,
			allowBlank: false,
			disabled: true
		});
		
		this.limPassword = new Ext.form.TextField({
			name: 'lim_password',
			id: 'lim_password',
			inputType:'password', 
			fieldLabel: tr.db_password,
			allowBlank: false,
			disabled: true
		});
		
		this.confirmLimPassword = new Ext.form.TextField({
	        fieldLabel: tr.db_confirm_password,	       
	        inputType:'password',
	        vtype: 'password',
	        initialPassField: 'lim_password',
	        disabled: true
	    });
		
		this.userFieldSet = new Ext.form.FieldSet({           
            title: tr.db_user_create,
            autoHeight:true,
            defaultType: 'textfield',            
			items: [
			        this.userType,
			        this.limUser,
			        this.limPassword,
			        this.confirmLimPassword
			]
		});
		
		Ext.apply(this, {
			labelWidth: 150,
			monitorValid: true,			
			layout: 'border',
			hideMode: "offsets",
			items: [{
				xtype: 'panel',
				frame: true,
				region: 'center',
				autoScroll: true,
				items: [
				        this.dbTypeAndNameFieldSet,
				        this.dbConnectionFieldset,
						this.userFieldSet
				]
			}],
			buttonAlign: 'right',
			buttons: [{
				id: 'card-prev-step2',
				iconCls: 'arrow_left',
				text: CMDBuild.Translation.configure.previous,
				handler: cardNav.createDelegate(this, [-1])	
			},
				this.nextStep,
				this.finishBtn
			]
		});
		
		CMDBuild.Configure.Step2.superclass.initComponent.apply(this, arguments);
		this.on('clientValidation', this.onValidation, this);
		this.dbType.on('select', this.onDbTypeSelect, this);
		this.userType.on('select', this.onUsertypeSelect, this) 
		
	},
	
	showNextButton: function(show) {
		this.finishBtn.setVisible(!show);
		this.nextStep.setVisible(show);
	},
	
	checkDbconnectionValidation: function() {
		if (this.host.isValid() &&
				this.port.isValid() &&
		        this.user.isValid() &&
		        this.password.isValid()) {
			this.connectionButton.enable()
		}
	},
	
	onDbTypeSelect: function(combo, record, index) {
		if (record.data.name == "empty") {
			this.showNextButton(true);
		} else {
			this.showNextButton(false);
		}
		if (record.data.name == "existing") {
			this.sharkSchema.disable();
			this.sharkSchema.setValue(false);
			this.usertypeDisableFields();
			this.userFieldSet.hide();
		} else {
			this.sharkSchema.enable();
			this.usertypeEnableFields(this.userType.getValue());
			this.userFieldSet.show();
		}
	},

	onUsertypeSelect: function(combo, record, index){
		this.usertypeEnableFields(record.data.name);
	},
	
	usertypeDisableFields: function(){
		this.limUser.disable();
		this.limPassword.disable();
		this.confirmLimPassword.disable();
		this.userType.disable();
	},
	
	usertypeEnableFields: function(userType){		
		this.usertypeDisableFields();
		this.userType.enable();
	    if (userType == "new_limuser") {        	
	    	this.limPassword.enable();
			this.confirmLimPassword.enable();
			this.limUser.enable();
		}	
	    if (userType == "limuser") {        	
	    	this.limPassword.enable();
	    	this.limUser.enable();
	    }
	},

	onValidation: function(form, valid) {
		Ext.ComponentMgr.get('card-next-step2').setDisabled(!valid);
		Ext.ComponentMgr.get('finish').setDisabled(!valid);
	},
	
    // ugly hack to avoid js exception: see http://extjs.com/forum/showthread.php?t=44418
	onRender: function() {
		CMDBuild.Configure.Step2.superclass.onRender.apply(this, arguments);
		Ext.apply(this.getForm(),{
           url:'services/json/configure/testconnection'
      });		
	},
	
	testConnection: function() {
		CMDBuild.log.info('testing ...');
		CMDBuild.log.info(this.getForm());
		var target = Ext.ComponentMgr.get('configure-viewport').getEl();
		var mask = new Ext.LoadMask(target);
		mask.show();
		Ext.Ajax.request({
			url:'services/json/configure/testconnection',
			params: {
				host: this.host.getValue(),
				port: this.port.getValue(),
				user: this.user.getValue(),
				password: this.password.getValue()
			},
			method:'POST',
			success:function(){
				Ext.Msg.show({
					title: CMDBuild.Translation.configure.step2.msg.title, 
					msg: CMDBuild.Translation.configure.step2.msg.msg, 
					buttons: Ext.MessageBox.OK 
				});
			},
			callback:function(form, action){
				target.unmask();
			}		
		});		
	},
	
	applyConfiguration: function(){
		params = {};
		var form1 = Ext.getCmp('card-1').getForm().getValues();
		var form2 = Ext.getCmp('card-2').getForm().getValues();
		var form3 = Ext.getCmp('card-3').getForm().getValues();
		for(key in form1) params[key] = form1[key];
		for(key in form2) params[key] = form2[key];
		for(key in form3) params[key] = form3[key];
		var target = Ext.ComponentMgr.get('configure-viewport').getEl();
		var mask = new Ext.LoadMask(target);
		mask.show();
		CMDBuild.Ajax.request({
	   		url: 'services/json/configure/apply',
	   		params: params,
			scope: this,
			success: function() {
				Ext.Msg.show({
					title: CMDBuild.Translation.configure.success.title,
					msg: CMDBuild.Translation.configure.success.text,
					buttons: Ext.MessageBox.OK,
					fn: function() { window.location = 'management.jsp' }
				});
			},
			callback: function() {			
				target.unmask();
			}
		});
	}
});
Ext.reg('configureStep2', CMDBuild.Configure.Step2);