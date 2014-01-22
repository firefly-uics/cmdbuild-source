(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksGridAndFormPanel", {
		extend: "Ext.panel.Panel",

		title: "@@ Task manager",
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			this.addButton = new Ext.button.Split({
				iconCls: 'add',
				text: "@@ Add Task",
				handler: function() {
					this.showMenu();
					//me.delegate.cmOn("onAddButtonClick", {});
				},
			    menu: new Ext.menu.Menu({
			        items: [
			            // these will render as dropdown menu items when the arrow is clicked:
			            {text: '@@ Mail', handler: function(){ me.delegate.cmOn("onAddButtonClick", {type: "Mail"}, -1); }},
			            {text: '@@ Event', handler: function(){ me.delegate.cmOn("onAddButtonClick", {type: "Event"}, -1); }}
			        ]
			    })
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