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

			this.columns = [
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
					width: '60px',
					align: 'center',
					dataIndex: CMDBuild.ServiceProxy.parameter.ACTIVE,
					renderer: me.activeGridColumnRenderer,
					fixed: true
				},
				{
					text: tr.start,
					width: '60px',
					align: 'center',
					renderer: function() {
						return '<img src="images/icons/control_play.png" title="' + tr.startLabel + '" alt="' + tr.start + '" />';
					},
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				},
				{
					text: tr.stop,
					width: '60px',
					align: 'center',
					renderer: function() {
						return '<img src="images/icons/control_stop.png" title="' + tr.stopLabel + '" alt="' + tr.stop + '" />';
					},
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}
			];

			this.store = CMDBuild.core.serviceProxy.CMProxyTasks.getStore(this.taskType);

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
			},

			show: function() {
//				this.store.load({
//					callback: function() {
//						me.getSelectionModel().select(0, true);
//					}
//				});
			},

			beforecellclick: function(grid, td, cellIndex, record, tr, rowIndex, e, eOpts) {
				switch (cellIndex) {
					case 4: {
						this.delegate.cmOn('onStartTask', {
							'record': record.raw,
							'index': rowIndex
						}, null);
					} break;

					case 5: {
						this.delegate.cmOn('onStopTask', {
							'record': record.raw,
							'index': rowIndex
						}, null);
					} break;
				}
			}
		},

		/**
		 * @param {Object} value
		 * Used to render active database value to add icon
		 */
		activeGridColumnRenderer: function(value) {
			if(typeof value === 'boolean') {
				if(value) {
					value = '<img src="images/icons/tick.png" alt="Is Default" />';
				} else {
					value = null;
				}
			}

			return value;
		}
	});

})();