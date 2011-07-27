(function() {
	Ext.define("CMDBuild.view.administration.classes.CMGeoAttributesPanel", {
		extend: "Ext.panel.Panel",
		
		constructor: function() {
			this.form = new CMDBuild.view.administration.classes.CMGeoAttributeForm({
				region: "center"
			});

			this.grid = new CMDBuild.view.administration.classes.CMGeoAttributesGrid({
				region: "north",
				split: true,
				height: "40%"
			});

			this.callParent(arguments);
		},
		
		initComponent: function() {
			
			Ext.apply(this, {
				layout: "border",
				items: [this.grid,this.form]
			});
			
			this.callParent(arguments);
		},
		
		onClassSelected: function(idClass) {
			if (CMDBuild.Config.gis.enabled) {
				this.enable();
				this.form.onClassSelected(idClass);
				this.grid.onClassSelected(idClass);
			} else {
				this.disable();
			}
		}
	})
})();