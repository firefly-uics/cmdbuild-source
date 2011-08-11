(function() {
	var tr = CMDBuild.Translation.administration.setup.gis,
		width =  CMDBuild.CM_SMALL_FIELD_WIDTH + 60;

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
				xtype: 'numberfield',
				name:'center.lat',
				fieldLabel: tr.center_lat,
				width: width
			},{
				xtype: 'numberfield',
				name:'center.lon',
				fieldLabel: tr.center_lon,
				width: width
			},{
				xtype: 'numberfield',
				name:'initialZoomLevel',
				fieldLabel: tr.initial_zoom,
				width: width,
				minValue : 0,
				maxValue : 25
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