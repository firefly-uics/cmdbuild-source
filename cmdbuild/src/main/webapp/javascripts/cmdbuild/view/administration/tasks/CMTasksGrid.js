(function() {

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				columns: [
					{
						text: '@@ Type',
						dataIndex: 'type',
						flex: 1
					},
					{
						text: '@@ Status',
						dataIndex: 'status',
						flex: 1
					},
					{
						text: '@@ Last Execution',
						dataIndex: 'last',
						flex: 1
					},
					{
						text: '@@ Next execution',
						dataIndex: 'next',
						flex: 1
					},
					{
						text: '@@ Start',
						dataIndex: 'start',
						width: '60px',
						align: 'center',
						renderer: function() {
							return '<img src="images/icons/arrow_right.png" title="@@ Start task" alt="@@ Start" />';
						},
						sortable: false,
						hideable: false,
						menuDisabled: true
					},
					{
						text: '@@ Stop',
						dataIndex: 'stop',
						width: '60px',
						align: 'center',
						renderer:  function() {
							return '<img src="images/icons/cross.png" title="@@ Stop task" alt="@@ Stop" />';
						},
						sortable: false,
						hideable: false,
						menuDisabled: true
					}
				],
//				store: CMDBuild.ServiceProxy.configuration.email.accounts.getStore()
				store: Ext.data.Store({
					autoLoad: false,
					model: 'CMDBuild.model.tasks.grid',
					proxy: {
						type: 'ajax',
						url: 'services/json/administration/task/taskmanager/gettaskslist',
						reader: {
							type: 'json',
							root: 'response'
						}
					},
					sorters: {
						property: 'last',
						direction: 'ASC'
					}
				})
			});

			this.callParent(arguments);
		},


		listeners: {
			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected', {
					'row': row,
					'record': record,
					'index': index
				}, null);
			},

			beforecellclick: function(grid, td, cellIndex, record, tr, rowIndex, e, eOpts) {
				switch (cellIndex) {
					case 5: {
						this.delegate.cmOn('onStartTask', {
							'record': record.raw,
							'index': rowIndex
						}, null);
					} break;

					case 6: {
						this.delegate.cmOn('onStopTask', {
							'record': record.raw,
							'index': rowIndex
						}, null);
					} break;
				}
			}
		},

		load: function(type) {
			var me = this;

			this.store.load({
				params: { 'type': type },
				scope: this,
				callback: function() {
					me.getSelectionModel().select(0, true);
				}
			});
		}
	});

})();