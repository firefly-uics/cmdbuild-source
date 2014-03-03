(function() {
	Ext.define("CMDBuild.view.administration.workflow.CMProcessAttributesPanel", {
		extend: "CMDBuild.view.administration.classes.CMClassAttributesPanel",

		onClassSelected: function(idClass) {
			this.formPanel.onClassSelected(idClass);
			this.gridPanel.onClassSelected(idClass);
		},

		// override
		buildFormPanel: function() {
			return new CMDBuild.view.administration.workflow.CMProcessAttributeForm({
				region: "center"
			});
		}
	});
})();