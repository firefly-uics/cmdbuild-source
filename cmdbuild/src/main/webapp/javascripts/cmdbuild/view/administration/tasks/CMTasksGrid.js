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

// TODO: maybe for a future implementation
//			this.pagingBar = Ext.create('Ext.toolbar.Paging', {
//				store: this.store,
//				displayInfo: true,
//				displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
//				emptyMsg: CMDBuild.Translation.common.display_topic_none
//			});

			Ext.apply(this, {
//				bbar: this.pagingBar,
				columns: this.gridColumns
			});

			this.callParent(arguments);
		},


		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(model, record, index, eOpts) {
				this.delegate.cmOn('onRowSelected');
			}
		},

		/**
		 * Used to render active value to add icon in grid
		 *
		 * @param (Boolean) value
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