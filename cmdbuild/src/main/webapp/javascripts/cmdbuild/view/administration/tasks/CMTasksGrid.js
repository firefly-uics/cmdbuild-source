(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksGrid", {
		extend: "Ext.grid.Panel",

		border: false,
		frame: false,
		cls: "cmborderbottom",
		listeners: {
		   select: function(row, record, index) {
			   this.delegate.cmOn("onRowSelected", {'row': row, 'record': record, 'index': index}, null);
		   }//,		   
//		   afterrender: function() {
//			   this.getSelectionModel().select(0);
//		   }

		},
		initComponent: function() {

			Ext.apply(this, {
				columns: [
					{ text: "@@ Id",  dataIndex: 'id', hidden: true },
					{ text: "@@ Type",  dataIndex: 'type', flex: 1 },
					{ text: "@@ Status", dataIndex: 'status', flex: 1 },
					{ text: "@@ Last Execution", dataIndex: 'last', flex: 1 },
					{ text: "@@ Next execution", dataIndex: 'next', flex: 1 },
					{ text: "@@ Start", dataIndex: 'start', width: '60px', align: "center", renderer: startRenderer, sortable: false, hideable: false, menuDisabled: true },
					{ text: "@@ Stop", dataIndex: 'stop', width: '60px', align: "center", renderer: stopRenderer, sortable: false, hideable: false, menuDisabled: true }
				],
//				store : CMDBuild.ServiceProxy.group.getUserStoreForGrid()
				store: Ext.data.Store({
					autoLoad: true,
					fields: ['type', 'status', 'last', 'next'],
					data: {
						'items': [
							{ 'id': 1, 'type': "Mail", 'status': 'In execution', 'last': '10/01/2013 08.00', 'next': '10/01/2013 16.00'},
							{ 'id': 2, 'type': "Mail", 'status': 'Stopped', 'last': '10/01/2013 08.00', 'next': '' },
							{ 'id': 3, 'type': "Mail", 'status': 'In execution', 'last': '10/01/2013 08.00', 'next': '10/01/2013 16.00' },
							{ 'id': 4, 'type': "Mail", 'status': 'Stopped', 'last': '10/01/2013 08.00', 'next': '' }
						]
					},
					proxy: {
						type: 'memory',
						reader: {
							type: 'json',
							root: 'items'
						}
					}
				})
			});

			this.callParent(arguments);
		}
	});

	/**
	 * @param {Database value} boolean
	 * Used to render isDefault database value to add icon
	 */
	function startRenderer() {
		return "<img src='images/icons/arrow_right.png' alt='@@ Start'/>";
	}
	function stopRenderer() {
		return "<img src='images/icons/cross.png' alt='@@ Stop'/>";
	}

})();