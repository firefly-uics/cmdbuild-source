(function() {
	Ext.define("CMDBuild.view.administration.bim.CMBIMPanel", {
		extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

		cmName: 'bim-project',

		title: "@@ BIM projects",

		addButtonText: "@@ Add Project",
		modifyButtonText: "@@ modify Project",
		removeButtonText: "@@ Remove Project",
		withRemoveButton: false,
		withEnableDisableButton: true
	});
})();
