(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,
		taskType: 'all', // default task type

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			var me = this;

			this.gridColumns = [
				{
					dataIndex: CMDBuild.ServiceProxy.parameter.ID,
					hidden: true
				},
				{
					text: tr.type,
					dataIndex: CMDBuild.ServiceProxy.parameter.TYPE,
					flex: 1
				},
				{
					text: CMDBuild.Translation.description_,
					dataIndex: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					flex: 4
				},
				{
					text: tr.status,
					width: 60,
					align: 'center',
					dataIndex: CMDBuild.ServiceProxy.parameter.ACTIVE,
					renderer: function(value, metaData, record) {
						return me.activeGridColumnRenderer(value, metaData, record);
					},
					fixed: true
				},
				{
					xtype: 'actioncolumn',
					width: 40,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
//					renderer: function(value, metaData, record) {
//						this.items = [me.actionGridColumnRenderer(value, metaData, record)];
//					},
					items: [
						{
							icon: 'images/icons/control_play.png',
							tooltip: tr.startLabel,
							handler: function(grid, rowIndex, colIndex) {
								var rec = grid.getStore().getAt(rowIndex);
								alert("Edit " + rec.get('firstname'));
							}
						},
						{
							icon: 'images/icons/control_stop.png',
							tooltip: tr.stopLabel,
							handler: function(grid, rowIndex, colIndex) {
								var rec = grid.getStore().getAt(rowIndex);
								alert("Terminate " + rec.get('firstname'));
							}
						}
					]
				}
			];

			this.gridStore = CMDBuild.core.serviceProxy.CMProxyTasks.getStore(this.taskType);

			Ext.apply(this, {
				columns: this.gridColumns,
				store: this.gridStore
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

			/**
			 * Event to load store on view display and first row selection as CMDbuild standard
			 */
			viewready: function() {
				var me = this;

				this.store.load({
					callback: function() {
						me.getSelectionModel().select(0, true);
					}
				});
			}
		},

		/**
		 * @param {Object} value
		 * Used to render active database value to add icon
		 */
		activeGridColumnRenderer: function(value, metaData, record) {
			if(typeof value === 'boolean') {
				if(value) {
					value = '<img src="images/icons/accept.png" alt="' + tr.running + '" />';
				} else {
					value = '<img src="images/icons/cancel.png" alt="' + tr.stopped + '" />';
				}
			}

			return value;
		},

		/**
		 * @param {Object} value
		 * Used to render active database value to add icon
		 */
//		actionGridColumnRenderer: function(value, metaData, record) {
//			var active = record.data.active;
//
//			if (typeof active === 'boolean') {
//				var startColumnItem = {
//						icon: 'images/icons/control_play.png',
//						tooltip: tr.startLabel,
//						handler: function(grid, rowIndex, colIndex) {
//							var rec = grid.getStore().getAt(rowIndex);
//							alert("Edit " + rec.get('firstname'));
//						}
//					},
//					stopColumnItem = {
//						icon: 'images/icons/control_stop.png',
//						tooltip: tr.stopLabel,
//						handler: function(grid, rowIndex, colIndex) {
//							var rec = grid.getStore().getAt(rowIndex);
//							alert("Terminate " + rec.get('firstname'));
//						}
//					};
//
//				return (active) ? stopColumnItem : startColumnItem;
//			}
//
//			_debug('CMTaskGrid rendering error: cannot render action column');
//		}
	});

})();
