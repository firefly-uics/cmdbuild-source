(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasks", {
		extend: "Ext.panel.Panel",

		delegate: undefined,

		title: '@@ Task manager',
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			this.addButton = Ext.create('Ext.button.Split', {
				iconCls: 'add',
				text: '@@ Add Task',
				handler: function() {
					this.showMenu();
				},
				menu: Ext.create('Ext.menu.Menu', {
					items: [
						// render as dropdown menu items when the arrow is clicked
						{
							text: '@@ Email',
							handler: function() {
								me.delegate.cmOn('onAddButtonClick', { type: 'email' }, -1);
							}
						},
						{
							text: '@@ Event',
							handler: function() {
								me.delegate.cmOn('onAddButtonClick', { type: 'event' }, -1);
							}
						},
						{
							text: '@@ Workflow',
							handler: function() {
								me.delegate.cmOn('onAddButtonClick', { type: 'workflow' }, -1);
							}
						},
					]
				})
			});

			this.grid = Ext.create('CMDBuild.view.administration.tasks.CMTasksGrid', {
				region: 'center'
			});

			this.form = Ext.create('CMDBuild.view.administration.tasks.CMTasksForm', {
				region: 'south',
				height: '70%'
			});

			Ext.apply(this, {
				tbar: [this.addButton],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();