(function() {
	var tr = CMDBuild.Translation.administration.setup.gis;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationGis", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		alias: "widget.configuregis",
		configFileName: 'gis',
		
		constructor: function() {
			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: tr.enable
			},{
				xtype: 'field',
				name:'center.lat',
				fieldLabel: tr.center_lat,
				width: 300
			},{
				xtype: 'field',
				name:'center.lon',
				fieldLabel: tr.center_lon,
				width: 300
			},{
				xtype: 'field',
				name:'initialZoomLevel',
				fieldLabel: tr.initial_zoom,
				width: 300
			}]

			this.callParent(arguments);
		},
		
		afterSubmit: function(conf) {
			CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis, conf);
			CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
			
			if (CMDBuild.Config.gis.enabled) {
				_CMMainViewportController.enableAccordionByName("gis");
			} else {
				_CMMainViewportController.disableAccordionByName("gis");
			}
		}
	});
})();