(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksGrid", {
		extend: "Ext.grid.Panel",

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

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
			var me = this,
				params = { 'type': type };

			this.store.load({
				params: params,
				scope: this,
				callback: function() {
					me.getSelectionModel().select(0, true);
				}
			});
		},

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{ text: '@@ Id', dataIndex: 'id', hidden: true },
					{ text: '@@ Type', dataIndex: 'type', flex: 1 },
					{ text: '@@ Status', dataIndex: 'status', flex: 1 },
					{ text: '@@ Last Execution', dataIndex: 'last', flex: 1 },
					{ text: '@@ Next execution', dataIndex: 'next', flex: 1 },
					{ text: '@@ Start', dataIndex: 'start', width: '60px', align: 'center', renderer: startRenderer, sortable: false, hideable: false, menuDisabled: true },
					{ text: '@@ Stop', dataIndex: 'stop', width: '60px', align: 'center', renderer: stopRenderer, sortable: false, hideable: false, menuDisabled: true }
				],

				store: Ext.data.Store({
					autoLoad: false,
					model: 'CMTasksModelForGrid',
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
		}
	});

	/**
	 * @param {Database value} boolean
	 * xxx
	 */
	function startRenderer() {
		return '<img src="images/icons/arrow_right.png" title="@@ Start task" alt="@@ Start" />';
	}
	function stopRenderer() {
		return '<img src="images/icons/cross.png" title="@@ Stop task" alt="@@ Stop" />';
	}

})();