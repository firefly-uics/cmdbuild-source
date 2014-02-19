(function() {
	var tr = CMDBuild.Translation.administration.modWorkflow;

	Ext.define("CMDBuild.view.administration.workflow.CMModProcess", {
		extend: "Ext.panel.Panel",
		cmName:'process',

		constructor: function() {

			this.addClassButton = new Ext.button.Button({
				iconCls : 'add',
				text : tr.add_process
			});

			this.printSchema = new CMDBuild.PrintMenuButton({
				text : CMDBuild.Translation.administration.modClass.print_schema,
				formatList: ['pdf', 'odt']
			});

			this.processForm = new CMDBuild.view.administration.workflow.CMProcessForm({
				title: tr.tabs.properties
			});

			this.attributesPanel = new CMDBuild.view.administration.workflow.CMProcessAttributesPanel({
				title: tr.tabs.attributes,
				border: false,
				disabled: true
			});

			this.domainGrid = new CMDBuild.Administration.DomainGrid({
				title : tr.tabs.domains,
				border: false,
				disabled: true
			});

			this.cronPanel = new CMDBuild.view.administration.workflow.CMCronPanel({
				title : tr.tabs.scheduling,
				border: false,
				disabled: true
			});

			this.tabPanel = new Ext.tab.Panel({
				frame: false,
				border: false,
				activeTab: 0,

				items: [
					this.processForm
					,this.attributesPanel
					,this.domainGrid
					,this.emailTemplatePanel
					,this.cronPanel
				]
			});

			Ext.apply(this, {
				tbar:[this.addClassButton, this.printSchema],
				title : tr.title,
				basetitle : tr.title+ ' - ',
				layout: 'fit',
				items: [this.tabPanel],
				frame: false,
				border: true
			});

			this.callParent(arguments);
		},

		onAddClassButtonClick: function() {
			this.tabPanel.setActiveTab(0);
		},

		onClassDeleted: function() {
			this.attributesPanel.disable();
		},

		onProcessSelected: Ext.emptyFn
	});
})();