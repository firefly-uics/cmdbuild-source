Ext.define("CMDBuild.view.administration.tasks.CMTasksGridAndFormPanel", {
	extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

	cmName:'tasksview',

	title: CMDBuild.Translation.views + " - " + "@@ Tasks",

	addButtonText: "@@ Add Task",
	modifyButtonText: "@@ Modify Task",
	removeButtonText: "@@ Remove Task"
});
