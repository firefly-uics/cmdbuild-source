(function() {
var tr = CMDBuild.Translation.administration.modWorkflow.scheduler;
Ext.define("CMDBuild.view.administration.workflow.CMCronPanel", {
	extend: "Ext.panel.Panel",
	
	initComponent: function() {
		this.jobGrid = new CMDBuild.view.administration.workflow.cron.CMJobGrid({
			region: "north",
			height: "30%",
			split: true
		});

		this.jobPanel = new CMDBuild.view.administration.workflow.cron.CMJobPanel({title: tr.job});
		this.jobParemetersGrid = new CMDBuild.view.administration.workflow.cron.CMJobParameterGrid({title: tr.params});

		this.tabPanel = new Ext.tab.Panel({
			region: "center",
			items: [this.jobPanel, this.jobParemetersGrid]
		})

		Ext.apply(this, {
			layout: "border",
			items: [this.jobGrid, this.tabPanel]
		});

		this.callParent();
	}
});

})();