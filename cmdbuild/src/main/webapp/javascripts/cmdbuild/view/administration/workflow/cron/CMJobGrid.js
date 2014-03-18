(function() {

	var tr = CMDBuild.Translation.administration.modWorkflow.scheduler;

	Ext.define('CMDBuild.view.administration.workflow.cron.CMJobGrid', {
		extend: 'Ext.grid.Panel',

//		translation: CMDBuild.Translation.administration.modWorkflow.scheduler,
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

//			this.addJobButton = new Ext.button.Button({
//				text: tr.addJob,
//				iconCls: 'add'
//			});

			Ext.apply(this, {
				store: this.store,
//				tbar : [ this.addJobButton ],
				columns: [
					{
						header: tr.description,
						flex: 1,
						sortable: true,
						dataIndex: 'description'
					},
					{
						header: tr.cronexpression,
						width: 75,
						sortable: true,
						dataIndex: 'cronExpression',
						flex: 1
					}
				],
				stripeRows: true
			});

			this.callParent(arguments);
		},

		load: function(processId) {
			var params = {};
			params[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(processId);

			this.store.load({
				params: params
			});
		},

		isSelected: function() {
			return this.getSelectionModel().isSelected();
		}
	});

})();