(function() {
	Ext.ns("CMDBuild.core.error");
	
	CMDBuild.core.error.tree = {
		WRONG_CMDOMAIN_MODEL: "You must to pass a CMDBuild.core.model.CMDomainModel",
		WRONG_CMMODEL_LIBRARY: "You must to pass a CMDBuild.core.model.CMDomainModelLibrary",
		NO_CONFIGURATION: function(className) {
			return "You are trying to instantiate a " + className+ " without passing a configuration";
		}
	};
	
	CMDBuild.core.error.model = {
		NO_CONFIGURATION: "The build method was called without configuaration",
		NO_CONFIGURATION_NAME: "The build configuration must have a name attribute",
		NO_CONFIGURATION_STRUCTURE: "The build configuration must have a structure attribute",
		CONFIGURATION_STRUCTURE_IS_NOT_OBJECT: "The structure attribute must is an object",
		NO_ATTRIBUTE_NAME: "I can not find the name of the attribue",
		SET_UNDEFINED_ATTR: "I can set an undefined attribute",
		NO_MODEL_NAME: "A model library need a model name to be istantiated",
		MODEL_NAME_IS_NOT_STRING: "The modelName must be a string",
		NO_KEY_ATTRIBUTE: "To build a library you must say tell the attribute to use as key",
		ADD_WRONG_MODEL_TO_LIBRARY: "You are trying to add a wrong model to the library",
		A_MODEL_IS_REQUIRED: "You have call the add method of a library without a model to add",
		GET_WITHOUT_ID: "The get method must have a id as parameter",
		REMOVE_WITHOUT_ID: "The remove method is called without id",
		REQUIRED_ATTRIBUTE: function(attrName) {
			return "The attribute "+ attrName +" is required";
		}
	};
})();