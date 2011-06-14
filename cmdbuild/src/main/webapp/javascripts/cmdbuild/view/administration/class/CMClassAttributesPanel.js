(function() {
	Ext.define("CMDBuild.view.administration.classes.CMClassAttributesPanel", {
		extend: "Ext.panel.Panel",
		
		constructor: function() {
			this.formPanel = new CMDBuild.view.administration.classes.CMAttributeForm({
				region: "center"
			});

			this.gridPanel = new CMDBuild.view.administration.classes.CMAttributeGrid({
				region: "north",
				split: true,
				height: "40%"
			});

			this.callParent(arguments);
		},
		
		initComponent: function() {
			
			Ext.apply(this, {
				layout: "border",
				items: [this.formPanel, this.gridPanel]
			});
			
			this.callParent(arguments);
		},
		
		onClassSelected: function(idClass) {
			this.formPanel.onClassSelected(idClass);
			this.gridPanel.onClassSelected(idClass);
		}
	})
})();