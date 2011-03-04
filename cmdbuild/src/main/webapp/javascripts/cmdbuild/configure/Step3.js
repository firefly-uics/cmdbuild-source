Ext.ns("CMDBuild.Configure");

CMDBuild.Configure.Step3 = Ext.extend( Ext.form.FormPanel, {

	initComponent: function() {
		var tr = CMDBuild.Translation.configure.step3;
		
		var cardNav = function(incr){
			var l = Ext.getCmp('configure_wizard_panel').getLayout();
			var i = l.activeItem.id.split('card-')[1];
			var next = parseInt(i) - 1 + incr;
			l.setActiveItem(next);
		};
		var applyConfiguration = function(){
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
				success: function(){
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
		};
		
		this.adminUser = new Ext.form.TextField({
			name: 'admin_user',
			fieldLabel: tr.admin_user,
			allowBlank: false,
			disabled: true
		});
		
		this.adminPassword = new Ext.form.TextField({
			name: 'admin_password',
			id: 'admin_password',
			inputType:'password', 
			allowBlank: false,
			fieldLabel: tr.admin_password,
			disabled: true
		});
		
		this.confirmAdminPassword = new Ext.form.TextField({
			name: '',
	        inputType:'password',
	        vtype: 'password',
	        initialPassField: 'admin_password',
	        fieldLabel: tr.confirm_password,
	        allowBlank: false,
	        disabled: true
	    });
		
		Ext.apply(this, {
			labelWidth: 150,
			monitorValid: true,
			layout: 'border',
			items: [{
				xtype: 'panel',
				frame: true,
				region: 'center',
				items: [{
					xtype:'fieldset',
		            title: tr.admin,
		            autoHeight:true,
		            defaultType: 'textfield',
		            collapsed: false,
					items: [
				        this.adminUser,
				        this.adminPassword,
				        this.confirmAdminPassword
					]
				}]
			}],
			buttonAlign: 'right',
			buttons: [{
				id: 'card-prev-step3',
				iconCls: 'arrow_left',
				text: CMDBuild.Translation.configure.previous,
				handler: cardNav.createDelegate(this, [-1])
					
			},{
				id: 'card-next-step3',
				iconCls: 'add',
				handler: applyConfiguration,
				text: CMDBuild.Translation.configure.finish,
				disabled: true
			}]
		});
		CMDBuild.Configure.Step3.superclass.initComponent.apply(this, arguments);
		this.on('clientValidation', this.onValidation, this);
		this.on('show', this.enableAllFields);
		this.on('hide', this.disableAllFields);
	},
	
	onRender: function() {
		CMDBuild.Configure.Step3.superclass.onRender.apply(this, arguments);
	},
	
	onValidation: function(form, valid) {
		Ext.ComponentMgr.get('card-next-step3').setDisabled(!valid);
	},
	
	enableAllFields: function() {
		this.adminUser.enable();
		this.adminPassword.enable();
		this.confirmAdminPassword.enable();
	},
	
	disableAllFields: function() {
		this.adminUser.disable();
		this.adminPassword.disable();
		this.confirmAdminPassword.disable();
	}
});

Ext.reg('configureStep3', CMDBuild.Configure.Step3);