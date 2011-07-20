(function() {
	var tr = CMDBuild.Translation.administration.setup.legacydms;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationAlfresco", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'legacydms',

		constructor: function() {
			this.items = [
			{
				xtype : 'fieldset',
				title : tr.general,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
				{
					fieldLabel : tr.enabled,
					xtype : 'xcheckbox',
					name : 'enabled'
				},
				{
					fieldLabel : tr.serverUrl,
					allowBlank : false,
					name : 'server.url',
					width: CMDBuild.CM_BIG_FIELD_WIDTH
				},
				{
					fieldLabel : tr.delay,
					allowBlank : false,
					xtype : 'numberfield',
					width: CMDBuild.CM_SMALL_FIELD_WIDTH + 60,
					name : 'delay'
				}]
			},
			{
				xtype : 'fieldset',
				title : tr.fileserver,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
					{
						fieldLabel : tr.fileserverType,
						allowBlank : false,
						name : 'fileserver.type',
						width: CMDBuild.CM_BIG_FIELD_WIDTH,
						disabled : true
					},
					{
						fieldLabel : tr.fileserverUrl,
						allowBlank : false,
						name : 'fileserver.url',
						width: CMDBuild.CM_BIG_FIELD_WIDTH
					},
					{
						fieldLabel : tr.fileserverPort,
						allowBlank : false,
						xtype : 'numberfield',
						name : 'fileserver.port',
						width: CMDBuild.CM_SMALL_FIELD_WIDTH + 60
					}
				]
			},
			{
				xtype : 'fieldset',
				title : tr.repository,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
				{
					fieldLabel : tr.repositoryFSPath,
					allowBlank : false,
					name : 'repository.fspath',
					width: CMDBuild.CM_BIG_FIELD_WIDTH
				}, 
				{
					fieldLabel : tr.repositoryWSPath,
					allowBlank : false,
					name : 'repository.wspath',
					width: CMDBuild.CM_BIG_FIELD_WIDTH
				}, 
				{
					fieldLabel : tr.repositoryApp,
					allowBlank : false,
					name : 'repository.app',
					width: CMDBuild.CM_MIDDLE_FIELD_WIDTH
				}]
			},
			{
				xtype : 'fieldset',
				title : tr.credential,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
				{
					fieldLabel : tr.credentialUser,
					allowBlank : false,
					name : 'credential.user',
					width: CMDBuild.CM_BIG_FIELD_WIDTH
				},
				{
					fieldLabel : tr.credentialPassword,
					allowBlank : false,
					inputType : 'password',
					name : 'credential.password',
					width: CMDBuild.CM_BIG_FIELD_WIDTH
				},
				{
					fieldLabel : tr.categoryLookup,
					xtype : 'xcombo',
					allowBlank : false,
					name : 'category.lookup',
					triggerAction : 'all',
					valueField : 'type',
					displayField : 'type',
					grow : true,
					triggerAction : 'all',
					store : CMDBuild.Cache.getLookupTypeLeavesAsStore(),
					queryMode : "local",
					width: CMDBuild.CM_BIG_FIELD_WIDTH
				}]
			}];

			this.callParent(arguments);
		}
	});

})();