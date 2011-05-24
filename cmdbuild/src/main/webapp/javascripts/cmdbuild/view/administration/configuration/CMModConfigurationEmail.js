(function() {
	var tr = CMDBuild.Translation.administration.setup.workflow;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmail", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		
		alias: "widget.configureemail",

		configFileName: 'email',
		constructor: function() {
			this.title = tr.email.title;
			this.items = [
			
			{
				xtype : 'fieldset',
				title : tr.email.title,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
					{
						fieldLabel : tr.email.address,
						vtype : 'emailaddrspec',
						allowBlank : true,
						name : 'email.address'
					},
					{
						fieldLabel : tr.email.username,
						allowBlank : true,
						name : 'email.username'
					},
					{
						fieldLabel : tr.email.password,
						allowBlank : true,
						name : 'email.password',
						inputType : 'password'
					}
				]
			},
			{
				xtype : 'fieldset',
				title : tr.email.outgoing_mail_srver,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
					{
						fieldLabel : tr.email.smtpserver,
						allowBlank : true,
						name : 'email.smtp.server'
					},
					{
						xtype : 'numberfield',
						fieldLabel : tr.email.port,
						allowBlank : true,
						minValue : 1,
						maxValue : 65535,
						name : 'email.smtp.port'
					},
					{
						fieldLabel : tr.email.ssl,
						xtype : 'xcheckbox',
						name : 'email.smtp.ssl'
					}
				]
			},
			{
				xtype : 'fieldset',
				title : tr.email.incoming_mail_server,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
					{
						fieldLabel : tr.email.imapserver,
						allowBlank : true,
						name : 'email.imap.server'
					},
					{
						xtype : 'numberfield',
						fieldLabel : tr.email.port,
						allowBlank : true,
						minValue : 1,
						maxValue : 65535,
						name : 'email.imap.port'
					},
					{
						fieldLabel : tr.email.ssl,
						xtype : 'xcheckbox',
						name : 'email.imap.ssl'
					}
				]
			}
			];
			
			this.callParent(arguments);
		}
	});
})();

//CMDBuild.Administration.ModSetupEmail = Ext.extend(CMDBuild.Administration.TemplateModSetup, {
//	id : 'modsetupemail',
//	configFileName: 'email',
//	modtype: 'modsetupemail',
//	translation: CMDBuild.Translation.administration.setup.workflow,
//	//custom
//	
//	initComponent: function() {
//		Ext.apply(this, {
//			title: this.translation.email.title,
//			formItems: 
//[{
//			    xtype: 'fieldset',
//			    title: this.translation.email.title,
//			    autoHeight: true,
//			    defaultType: 'textfield',
//			    items: [{
//			        fieldLabel: this.translation.email.address,
//			        vtype: 'emailaddrspec',
//			        allowBlank: true,
//			        name: 'email.address'
//				},{
//			        fieldLabel: this.translation.email.username,
//			        allowBlank: true,
//			        name: 'email.username'
//				},{
//			        fieldLabel:this.translation.email.password,
//			        allowBlank: true,
//			        name: 'email.password',
//			        inputType: 'password'
//				}]
//			},{
//				xtype: 'fieldset',
//			    title: this.translation.email.outgoing_mail_server,
//			    autoHeight: true,
//			    defaultType: 'textfield',
//			    items: [{
//			        fieldLabel: this.translation.email.smtpserver,
//			        allowBlank: true,
//			        name: 'email.smtp.server'
//				},{
//					xtype: 'numberfield',
//			        fieldLabel: this.translation.email.port,
//			        allowBlank: true,
//			        minValue: 1,
//				    maxValue: 65535,
//			        name: 'email.smtp.port'
//				},{
//					fieldLabel: this.translation.email.ssl,
//			        xtype: 'xcheckbox',
//			        name: 'email.smtp.ssl'
//				}]
//			},{
//				xtype: 'fieldset',
//			    title: this.translation.email.incoming_mail_server,
//			    autoHeight: true,
//			    defaultType: 'textfield',
//			    items: [{
//			        fieldLabel: this.translation.email.imapserver,
//			        allowBlank: true,
//			        name: 'email.imap.server'
//				},{
//					xtype: 'numberfield',
//			        fieldLabel: this.translation.email.port,
//			        allowBlank: true,
//			        minValue: 1,
//				    maxValue: 65535,
//			        name: 'email.imap.port'
//				},{
//					fieldLabel: this.translation.email.ssl,
//			        xtype: 'xcheckbox',
//			        name: 'email.imap.ssl'
//				}]
//			}]
//		})
//		CMDBuild.Administration.ModSetupEmail.superclass.initComponent.apply(this, arguments);
//    }
//});