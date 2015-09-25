(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.workflow.CMProcessTasksGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			this.gridColumns = [
				{
					dataIndex: CMDBuild.core.constants.Proxy.ID,
					hidden: true
				},
				{
					text: CMDBuild.Translation.descriptionLabel,
					dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
					flex: 2
				},
				{
					text: CMDBuild.Translation.active,
					width: 60,
					align: 'center',
					dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					scope: this,
					renderer: function(value, metaData, record) {
						return this.activeGridColumnRenderer(value, metaData, record);
					}
				}
			];

			Ext.apply(this, {
				columns: this.gridColumns
			});

			this.callParent(arguments);
		},


		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick', {
					id: record.get(CMDBuild.core.constants.Proxy.ID),
					type: 'workflow'
				});
			},

			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected');
			}
		},

		/**
		 * Used to render active database value to add icon
		 *
		 * @param (Boolean) value
		 */
		activeGridColumnRenderer: function(value, metaData, record) {
			return value ? '<img src="images/icons/accept.png" alt="' + tr.running + '" />' : '<img src="images/icons/cancel.png" alt="' + tr.stopped + '" />';
		}
	});

})();