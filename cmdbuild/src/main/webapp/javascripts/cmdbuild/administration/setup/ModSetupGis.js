CMDBuild.Administration.ModSetupGis = Ext.extend(CMDBuild.Administration.TemplateModSetup, {
	id : 'modsetupgis',
	configFileName: 'gis',
	modtype: 'modsetupgis',
	translation: CMDBuild.Translation.administration.setup.gis,
	//custom

	initComponent: function() {
		Ext.apply(this, {
			title: this.translation.title,
			formItems: [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: this.translation.enable
			},{
				xtype: 'field',
				name:'center.lat',
				fieldLabel: this.translation.center_lat,
				width: 300
			},{
				xtype: 'field',
				name:'center.lon',
				fieldLabel: this.translation.center_lon,
				width: 300
			},{
				xtype: 'field',
				name:'initialZoomLevel',
				fieldLabel: this.translation.initial_zoom,
				width: 300
			}]
		});
		CMDBuild.Administration.ModSetupGis.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-config-update-'+this.configFileName, function(config) {
			CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis, config);
            CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
		}, this);
    }
});