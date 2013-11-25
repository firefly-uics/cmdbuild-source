(function() {
	Ext.define("CMDBuild.view.administration.bim.CMBIMPanel", {
		extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

		title: CMDBuild.Translation.bim + " " + CMDBuild.Translation.projects,

		addButtonText: CMDBuild.Translation.addProject,
		modifyButtonText: CMDBuild.Translation.modifyProject,
		removeButtonText: CMDBuild.Translation.removeProject,
		withRemoveButton: false,
		withEnableDisableButton: true
	});
})();
