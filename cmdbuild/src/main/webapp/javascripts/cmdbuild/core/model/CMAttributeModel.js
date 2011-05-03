(function() {
	Ext.ns("CMDBuild.core.model");
	/**
	 * @class CMAttributeModel
	 * 
	 * This class define the structure of an attribute
	 * of a class, process or domain
	 */
	CMDBuild.core.model.CMAttributeModel = CMDBuild.core.model.CMModelBuilder.build({
		name: "CMDBuild.core.model.CMAttributeModel",
		structure: {
			active: {required: true},
			description: {required: true},
			name: {
				required: true,
				immutable: true
			},
			unique: {required: true},
			notnull: {required: true},
			shownAsGridColumn: {required: true},
			editingMode: {required: true},
			type: {
				required: true,
				immutable: true
			},
			stringLength: {required: false},
			decimalPrecision: {required: false},
			decimalScale: {required: false},
			lookupType: {
				required: false,
				immutable: true
			},
			referenceClass: {
				required: false,
				immutable: true
			},
			foreignKey: {
				required: false,
				immutable: true
			}
		}
	});
	
	CMDBuild.core.model.CMAttributeModel.buildFromJson = function(json) {
		var conf = {
			active: json.isactive,
			description: json.description,
			name: json.name,
			unique: json.isunique,
			notnull: json.isnotnull,
			shownAsGridColumn: json.isbasedsp,
			editingMode: json.fieldmode,
			type: json.type,
			stringLength: json.len,
			decimalPrecision: json.precision,
			decimalScale: json.scale,
			lookupType: json.lookup,
			referenceClass: json.referencedIdClass,
			foreignKey: json.fkDestination
		}

		return new CMDBuild.core.model.CMAttributeModel(conf);
	}
})();