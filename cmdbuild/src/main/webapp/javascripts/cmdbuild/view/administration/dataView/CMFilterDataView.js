Ext.define("CMDBuild.view.administration.dataview.CMFilterDataView", {
	extend: "CMDBuild.view.administration.common.CMGridAndFormPanel",

	cmName:'filterdataview',

	title: CMDBuild.Translation.views + " - " + CMDBuild.Translation.filterView,

	addButtonText: CMDBuild.Translation.addView,
	modifyButtonText: CMDBuild.Translation.modifyView,
	removeButtonText: CMDBuild.Translation.removeView
});
