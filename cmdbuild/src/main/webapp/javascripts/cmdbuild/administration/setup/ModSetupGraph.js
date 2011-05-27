CMDBuild.Administration.ModSetupGraph = Ext.extend(CMDBuild.Administration.TemplateModSetup, {
	id : 'modsetupgraph',
	configFileName: 'graph',
	modtype: 'modsetupgraph',
	translation: CMDBuild.Translation.administration.setup.graph,

	initComponent: function() {
		Ext.apply(this, {
			title: this.translation.title,
			formItems: [{
			    xtype: 'numberfield',
			    fieldLabel: this.translation.baseLevel,
			    allowBlank: false,
			    minValue: 1,
			    maxValue: 5,
			    name: 'baseLevel'
			},{
			    xtype: 'numberfield',
			    fieldLabel: this.translation.extensionMaximumLevel,
			    allowBlank: false,
			    minValue: 1,
			    maxValue: 5,
			    name: 'extensionMaximumLevel'
			},{
			    xtype: 'numberfield',
			    fieldLabel: this.translation.clusteringThreshold,
			    allowBlank: false,
			    minValue: 2,
			    maxValue: 20,
			    name: 'clusteringThreshold'
			}]
		});
		
		CMDBuild.Administration.ModSetupGraph.superclass.initComponent.apply(this, arguments);
    }
});