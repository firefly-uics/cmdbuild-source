(function() {
	Ext.ns("CMDBuild.core.model");
	/**
	 * @constant CMDBuild.core.model.CMDomainModelLibrary
	 * 
	 * Define the library to manage the CMDomain objects
	 * 
	 * @see CMDBuild.core.model.CMModelLibraryBuilder
	 */
	var modelName = CMDBuild.core.model.CMDomainModel.NAME;
	CMDBuild.core.model.CMDomainModelLibrary = CMDBuild.core.model.CMModelLibraryBuilder.build({
		modelName: modelName,
		keyAttribute: "id"
	});
	CMDBuild.core.model.CMDomainModelLibrary.NAME = "CMDBuild.core.model.CMDomainModelLibrary";
	CMDBuild.core.model.CMDomainModelLibrary.prototype.NAME = CMDBuild.core.model.CMDomainModelLibrary.NAME
	CMDomainModelLibrary = new CMDBuild.core.model.CMDomainModelLibrary();
})();