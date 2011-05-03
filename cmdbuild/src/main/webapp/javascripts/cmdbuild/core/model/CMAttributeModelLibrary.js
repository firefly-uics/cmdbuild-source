(function() {
	Ext.ns("CMDBuild.core.model");
	
	var modelName = CMDBuild.core.model.CMAttributeModel.NAME;

	CMDBuild.core.model.CMAttributeModelLibrary = CMDBuild.core.model.CMModelLibraryBuilder.build({
		modelName: modelName,
		keyAttribute: "name"
	});
	
	CMDBuild.core.model.CMAttributeModelLibrary.NAME = "CMDBuild.core.model.CMAttributeModelLibrary";
	CMDBuild.core.model.CMAttributeModelLibrary.prototype.NAME = CMDBuild.core.model.CMAttributeModelLibrary.NAME;
	
	CMDBuild.core.model.CMAttributeModelLibrary.prototype.asStore = function() {
		if (this.store == undefined) {
			this.store = new Ext.data.JsonStore({
				fields: (function() {
					var fields = [];
					for (var f in CMDBuild.core.model.CMAttributeModel.STRUCTURE) {
						fields.push(f);
					}
					return fields;
				})()
			});

			for (var key in this.map) {
				this.store.add(this.map[key].getRecord());
			}

			this.onAdd = function(model) {
				this.store.add(model.getRecord());
			};

			this.onRemove = function(id) {
				var recordIndex = this.store.find(this.KEY_ATTRIBUTE, id);
				this.store.removeAt(recordIndex);
			};
		}

		return this.store;
	};
})();