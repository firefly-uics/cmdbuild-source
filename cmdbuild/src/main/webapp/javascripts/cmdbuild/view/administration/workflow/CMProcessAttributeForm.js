(function() {

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define("CMDBuild.view.administration.workflow.CMProcessAttributeForm", {
		extend: "CMDBuild.view.administration.classes.CMAttributeForm",
		
		// override
		buildBasePropertiesPanel: function() {
			this.baseProperties = new Ext.form.FieldSet({
//				title : tr.baseProperties,
				autoScroll : true,
				defaultType : "textfield",
				flex: 1,
				items : [
					this.attributeName,
					this.attributeDescription,
					this.isBasedsp,
					this.attributeUnique,
					this.attributeNotNull,
					this.isActive,
					{
						xtype: "hidden",
						name: "meta"
					},
					this.fieldMode
				]
			});
		},
		
		// override
		takeDataFromCache: function(idClass) {
			return _CMCache.getProcessById(idClass);
		}
	});
})();