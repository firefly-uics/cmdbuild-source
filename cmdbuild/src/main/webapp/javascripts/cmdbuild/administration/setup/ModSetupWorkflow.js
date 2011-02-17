CMDBuild.Administration.ModSetupWorkflow = Ext.extend(CMDBuild.Administration.TemplateModSetup, {
	id : 'modsetupworkflow',
	configFileName: 'workflow',
	modtype: 'modsetupworkflow',
	translation: CMDBuild.Translation.administration.setup.workflow,
	//custom
	afterSubmit: function() {		
		var f = this.getForm();
		var en = f.findField("enabled");
		CMDBuild.Config.workflow.enabled = en.getValue();
	},
	initComponent: function() {
		Ext.apply(this, {
			title: this.translation.title,
			formItems: [{
				xtype: 'fieldset',
			    title: this.translation.general,
			    autoHeight: true,
			    defaultType: 'textfield',
			    items: [{
			        fieldLabel: this.translation.enabled,
			        xtype: 'xcheckbox',
			        name: 'enabled'
				},{
			        fieldLabel: this.translation.endpoint,
			        allowBlank: false,
			        name: 'endpoint',
			        width: 450
				}]
			},{
			    xtype: 'fieldset',
			    title: this.translation.credential,
			    autoHeight: true,
			    defaultType: 'textfield',
			    items: [{
			        fieldLabel: this.translation.user,
			        allowBlank: false,
			        name: 'user'
				},{
			        fieldLabel: this.translation.password,
			        allowBlank: false,
			        name: 'password',
			        inputType: 'password'
				},{
			        fieldLabel: this.translation.engine,
			        allowBlank: false,
			        name: 'engine',
			        disabled: true
				},{
			        fieldLabel: this.translation.scope,
			        allowBlank: true,
			        name: 'scope',
			        disabled: true
				}]
			}]
		})
		CMDBuild.Administration.ModSetupWorkflow.superclass.initComponent.apply(this, arguments);
    }
});