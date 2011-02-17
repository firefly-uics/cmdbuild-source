CMDBuild.Administration.ModLegacydms = Ext.extend(CMDBuild.Administration.TemplateModSetup, {
	id : 'modsetupalfresco',
	configFileName: 'legacydms',
	modtype: 'modsetupalfresco',
	translation: CMDBuild.Translation.administration.setup.legacydms,
	//custom
	
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
			        fieldLabel: this.translation.serverUrl,
			        allowBlank: false,
			        name: 'server.url',
			        width: 450
				},{
			        fieldLabel: this.translation.delay,
			        allowBlank: false,
			        xtype: 'numberfield',
			        name: 'delay'    
				}]
  			},{
			    xtype: 'fieldset',
			    title: this.translation.fileserver,
			    autoHeight: true,
			    defaultType: 'textfield',
			    items: [{
				    fieldLabel: this.translation.fileserverType,
				    allowBlank: false,
				    name: 'fileserver.type',
				    disabled: true
				},{
				    fieldLabel: this.translation.fileserverUrl,
				    allowBlank: false,
				    name: 'fileserver.url',
				    width: 450
				},{
				    fieldLabel: this.translation.fileserverPort,
				    allowBlank: false,
				    xtype: 'numberfield',
				    name: 'fileserver.port'
				}]
			},{
			    xtype: 'fieldset',
			    title: this.translation.repository,
			    autoHeight: true,
			    defaultType: 'textfield',
			    items: [
			      {
			        fieldLabel: this.translation.repositoryFSPath,
			        allowBlank: false,
			        name: 'repository.fspath',
			        width: 450
			      },{
			        fieldLabel: this.translation.repositoryWSPath,
			        allowBlank: false,
			        name: 'repository.wspath',
			        width: 450
			      },{
			        fieldLabel: this.translation.repositoryApp,			       
			        allowBlank: false,
			        name: 'repository.app'
			      }]
  			},{
			    xtype: 'fieldset',
			    title: this.translation.credential,
			    autoHeight: true,
			    defaultType: 'textfield',
			    items: [{
			        fieldLabel: this.translation.credentialUser,
			        allowBlank: false,
			        name: 'credential.user'	
			    },{
			        fieldLabel: this.translation.credentialPassword,
			        allowBlank: false,
			        inputType: 'password',
			        name: 'credential.password'
				},{
			        fieldLabel: this.translation.categoryLookup,
			        xtype: 'xcombo',
			        allowBlank: false,
			        name : 'category.lookup',
			        triggerAction : 'all',
			        valueField : 'type',
			        displayField : 'type',
			        grow: true,
			        store: CMDBuild.Cache.getLookupTypeLeavesAsStore(),
			        mode: "local"
				}]
  			}]
		})
		CMDBuild.Administration.ModLegacydms.superclass.initComponent.apply(this, arguments);
    }
});