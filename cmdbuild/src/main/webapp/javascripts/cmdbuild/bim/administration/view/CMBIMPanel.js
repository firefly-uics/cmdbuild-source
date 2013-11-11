(function() {
	Ext.define("CMDBuild.view.administration.bim.CMBIMPanel", {
		extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

		title: "@@ BIM projects",

		addButtonText: "@@ Add Project",
		modifyButtonText: "@@ modify Project",
		removeButtonText: "@@ Remove Project",
		withRemoveButton: false,
		withEnableDisableButton: true
	});
})();
