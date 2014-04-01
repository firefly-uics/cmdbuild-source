(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	Ext.define('CMDBuild.view.administration.tasks.CMTasks', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		title: tr.title,
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			this.addButton = Ext.create('Ext.button.Split', {
				iconCls: 'add',
				text: tr.add,
				handler: function() {
					this.showMenu();
				},
				menu: Ext.create('Ext.menu.Menu', { // Rendered as dropdown menu on button click
					items: [
						{
							text: tr.tasksTypes.email,
							handler: function() {
								me.delegate.cmOn('onAddButtonClick', { type: 'email' });
							}
						},
						{
							text: tr.tasksTypes.event,
							menu: [
								{
									text: tr.tasksTypes.eventTypes.asynchronous,
									handler: function() {
										me.delegate.cmOn('onAddButtonClick', { type: 'event_asynchronous' });
									}
								},
								{
									text: tr.tasksTypes.eventTypes.synchronous,
									handler: function() {
										me.delegate.cmOn('onAddButtonClick', { type: 'event_synchronous' });
									}
								},
							]
						},
						{
							text: tr.tasksTypes.workflow,
							handler: function() {
								me.delegate.cmOn('onAddButtonClick', { type: 'workflow' });
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