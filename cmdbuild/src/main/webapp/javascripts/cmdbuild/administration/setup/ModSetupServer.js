CMDBuild.Administration.ModSetupServer = Ext.extend(CMDBuild.ModPanel, {
	id : 'modsetupserver',
	configFileName: 'server',
	modtype: 'modsetupserver',
	translation: CMDBuild.Translation.administration.setup.server,

	initComponent: function() {
		Ext.apply(this, {
			title: this.translation.title,
			frame: true,
			labelWidth: 300,
			defaultType : 'textfield',
			items: [{
			    xtype: 'fieldset',
			    title: this.translation.cache_management,
			    autoHeight: true,
			    layout: 'column',
			    items: [{
					xtype: 'button',
					text: this.translation.clear_cache,
					handler: function() {
						CMDBuild.Ajax.request({
							url : 'services/json/utils/clearcache',
							loadMask: true,
			                success: CMDBuild.Msg.success
				  	 	});
					}
			    }]
			},{
			    xtype: 'fieldset',
			    title: this.translation.servicesync,
			    autoHeight: true,
			    layout: 'column',
			    items: [{
					xtype: 'button',
					text: this.translation.wfsync,
					handler: function() {
						CMDBuild.Ajax.request({
							url : 'services/json/schema/modworkflow/removeallinconsistentprocesses',
							loadMask: true,
			                success: CMDBuild.Msg.success
				  	 	});
					}
			    }]
			}]
		});
		CMDBuild.Administration.ModSetupServer.superclass.initComponent.apply(this, arguments);
    }
});
