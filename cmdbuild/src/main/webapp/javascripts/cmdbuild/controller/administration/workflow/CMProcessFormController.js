(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;
	
	Ext.define("CMDBuild.controller.administration.workflow.CMProcessFormController", {
		extend: "CMDBuild.controller.administration.classes.CMClassFormController",

		constructor: function(view) {
			this.callParent(arguments);
		},

		onProcessSelected: function(id) {
			this.selection = _CMCache.getProcessById(id);
			if (this.selection) {
				this.view.onClassSelected(this.selection);
			}
		},
		
		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.onAddClassButtonClick();
		},

		// override
		buildSaveParams: function() {
			var params = this.callParent(arguments)
			params.isprocess = true;

			return params;
		},

		//override
		saveSuccessCB: function(r) {
			var result = Ext.JSON.decode(r.responseText);
			this.selection = _CMCache.onProcessSaved(result.table);
		},
		
		//override
		deleteSuccessCB: function(r) {
			var removedClassId = this.selection.get("id");
			_CMCache.onProcessDeleted(removedClassId);

			this.selection = null;
		}

	});

})();