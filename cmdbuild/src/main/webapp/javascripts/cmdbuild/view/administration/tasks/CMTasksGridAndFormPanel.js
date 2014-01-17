(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksGridAndFormPanel", {
		extend: "Ext.panel.Panel",

		title: "@@ Task manager",
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: "@@ Add Task",
				handler: function() {
					me.delegate.cmOn("onAddButtonClick", {});
				}
			});

			this.taskGrid = new CMDBuild.view.administration.tasks.CMTasksGrid({
				region: 'center'
			});

			this.taskForm = new CMDBuild.view.administration.tasks.CMTasksPanel({
				region: 'south',
				height: '70%'
			});

			Ext.apply(this, {
				tbar: [this.addButton],
				items: [this.taskGrid, this.taskForm]
			});
			this.callParent(arguments);
		}
	});
})();