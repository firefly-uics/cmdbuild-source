(function() {
	var tr = CMDBuild.Translation.administration.setup.graph,
		width =  CMDBuild.CM_SMALL_FIELD_WIDTH + 60;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationGraph", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'graph',
		
		constructor: function() {
			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: CMDBuild.Translation.administration.setup.gis.enable
			}, {
				xtype : 'numberfield',
				fieldLabel : tr.baseLevel,
				width: width,
				allowBlank : false,
				minValue : 1,
				maxValue : 5,
				name : 'baseLevel'
			}, {
				xtype : 'numberfield',
				fieldLabel : tr.extensionMaximumLevel,
				width: width,
				allowBlank : false,
				minValue : 1,
				maxValue : 5,
				name : 'extensionMaximumLevel'
			}, {
				xtype : 'numberfield',
				fieldLabel : tr.clusteringThreshold,
				width: width,
				allowBlank : false,
				minValue : 2,
				maxValue : 20,
				name : 'clusteringThreshold'
			} ]
			this.callParent(arguments);
		}
	});
})();