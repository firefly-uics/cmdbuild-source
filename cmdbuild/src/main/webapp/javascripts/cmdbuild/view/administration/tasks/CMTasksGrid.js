(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

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
					text: tr.active,
					width: 60,
					align: 'center',
					dataIndex: CMDBuild.ServiceProxy.parameter.ACTIVE,
					renderer: function(value, metaData, record) {
						return me.activeGridColumnRenderer(value, metaData, record);
					},
					hideable: false,
					menuDisabled: true,
					fixed: true
				},
				{
					xtype: 'actioncolumn',
					width: 40,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							icon: 'images/icons/control_play.png',
							tooltip: tr.startLabel,
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								me.delegate.cmOn('onStartButtonClick', record);
							},
							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return record.get(CMDBuild.ServiceProxy.parameter.ACTIVE);
							}
						},
						{
							icon: 'images/icons/control_stop.png',
							tooltip: tr.stopLabel,
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								me.delegate.cmOn('onStopButtonClick', record);
							},
							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return !record.get(CMDBuild.ServiceProxy.parameter.ACTIVE);
							}
						}
					]
				}
			];

			Ext.apply(this, {
				columns: this.gridColumns
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
		}
	});

})();