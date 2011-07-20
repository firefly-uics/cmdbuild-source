(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.view.administration.workflow.CMProcessForm", {
		extend : "CMDBuild.view.administration.classes.CMClassForm",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		initComponent : function() {
			this.callParent(arguments);

			this.typeCombo.hide();
		},

		//override
		setDefaults: function() {
			this.isActive.setValue(true);
			this.inheriteCombo.setValue(_CMCache.getActivityRootId())
		},

		//override
		buildInheriteComboStore: function() {
			return _CMCache.getSuperProcessAsStore();
		}

	});
})();