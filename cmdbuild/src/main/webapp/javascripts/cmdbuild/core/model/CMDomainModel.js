(function() {
	Ext.ns("CMDBuild.core.model");
	/**
	 * @class CMDomainModel
	 * 
	 * This class define the structure of a domain
	 * between two tables. All his attributes are required
	 */
	CMDBuild.core.model.CMDomainModel = CMDBuild.core.model.CMModelBuilder.build({
		name: "CMDBuild.core.model.CMDomainModel",
		structure: {
			active: {required: true},
			id: {required: true},
			cardinality: {
				required: true,
				immutable: true
			},
			nameClass1: {required: true},
			nameClass2: {required: true},
			idClass1: {
				required: true,
				immutable: true
			},
			idClass2: {
				required: true,
				immutable: true
			},
			classType: {required: true},
			name: {
				required: true,
				immutable: true
			},
			createPrivileges: {required: true},
			writePrivileges: {required: true},
			isMasterDetail: {required: true},
			description: {required: true},
			directDescription: {required: true},
			reverseDescription: {required: true}
		}
	});
	
	CMDBuild.core.model.CMDomainModel.buildFromJSON = function(json) {
		var conf = {
			active: json.active,
			id: json.idDomain,
			cardinality: json.cardinality,
			nameClass1: json.class1,
			nameClass2: json.class2,
			idClass1: json.class1id,
			idClass2: json.class2id,
			classType: json.classType,
			name: json.name,
			createPrivileges: json.priv_create,
			writePrivileges: json.priv_write,
			isMasterDetail: json.md,
			description: json.description,
			directDescription: json.descrdir,
			reverseDescription: json.descrinv
		};
		return new CMDBuild.core.model.CMDomainModel(conf);
	};
})();