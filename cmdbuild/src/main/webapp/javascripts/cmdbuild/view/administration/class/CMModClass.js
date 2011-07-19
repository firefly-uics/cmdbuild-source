(function() {
	var tr = CMDBuild.Translation.administration.modClass;

	Ext.define("CMDBuild.view.administration.classes.CMModClass", {
		extend: "Ext.panel.Panel",

		NAME: "CMModClass",
		cmName:'class',

		constructor: function() {

			this.addClassButton = new Ext.button.Button({
				iconCls : 'add',
				text : tr.add_class
			});

			this.printSchema = new CMDBuild.PrintMenuButton({
				text : tr.print_schema,
				formatList: ['pdf', 'odt']
			});

			this.classForm = new CMDBuild.view.administration.classes.CMClassForm({
				title: tr.tabs.properties,
				border: false
			});

			this.attributesPanel = new CMDBuild.view.administration.classes.CMClassAttributesPanel({
				title: tr.tabs.attributes,
				border: false
			})

			this.domainGrid = new CMDBuild.Administration.DomainGrid({
				title : tr.tabs.domains,
				border: false
			});

			this.geoAttributesPanel = new CMDBuild.view.administration.classes.CMGeoAttributesPanel({
				title: tr.tabs.geo_attributes
			});

			this.layerVisibilityGrid = new CMDBuild.Administration.LayerVisibilityGrid({
				title: tr.layers,
				withCheckToHideLayer: true
			});

			this.tabPanel = new Ext.tab.Panel({
				frame: false,
				border: false,
				activeTab: 0,

				items: [
				 this.classForm
				,this.attributesPanel
				,this.domainGrid
				,this.layerVisibilityGrid
				,this.geoAttributesPanel
				]
			});

			Ext.apply(this, {
				tbar:[this.addClassButton, this.printSchema],
				title : tr.title,
				basetitle : tr.title+ ' - ',
				layout: 'fit',
				items: [this.tabPanel],
				frame: false,
				border: true
			});

			this.callParent(arguments);
		},
	
		onAddClassButtonClick: function() {
			this.tabPanel.setActiveTab(0);
		},

		onClassDeleted: function() {
			this.attributesPanel.disable();
			this.geoAttributesPanel.disable();
			this.domainGrid.disable();
            this.layerVisibilityGrid.disable();
		},

		onClassSelected: function(selection) {
            if (CMDBuild.Config.gis.enabled) {
				this.layerVisibilityGrid.enable();
                this.layerVisibilityGrid.onClassSelected(selection);
			} else {
				this.layerVisibilityGrid.disable();
			}

		}
	});
})();