Ext.define("CMDBuild.view.administration.workflow.cron.CMJobGrid", {
	extend: "Ext.grid.Panel",
	translation: CMDBuild.Translation.administration.modWorkflow.scheduler,
	initComponent: function() {

		this.store = new Ext.data.JsonStore({
			proxy: {
				type: 'ajax',
				url: 'services/json/schema/scheduler/listprocessjobs',
				reader: {
					type: 'json',
					root: 'rows'
				}
			},
			fields: ['description','params', 'cronExpression', 'id'],
			remoteSort: this.remoteSort
		});

		this.addJobButton = new Ext.button.Button({
			text: this.translation.addJob,
			iconCls: 'add'
		});

		Ext.apply(this, {
			store : this.store,
			tbar : [ this.addJobButton ],
			columns : [ {
				header : this.translation.description,
				flex: 1,
				sortable : true,
				dataIndex : 'description'
			}, {
				header : this.translation.cronexpression,
				width : 75,
				sortable : true,
				dataIndex : 'cronExpression',
				flex: 1
			} ],
			stripeRows : true
		});

		this.callParent(arguments);
	},

	load: function(processId) {
		this.store.load({
			params: {
				idClass: processId
			}
		});
	},
	
	isSelected: function() {
		return this.getSelectionModel().isSelected();
	}
});