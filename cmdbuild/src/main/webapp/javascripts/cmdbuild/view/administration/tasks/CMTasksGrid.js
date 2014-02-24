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

			Ext.apply(this, {
				columns: [
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
						dataIndex: CMDBuild.ServiceProxy.parameter.STATUS,
						flex: 1
					},
					{
						text: tr.start,
						width: '60px',
						align: 'center',
						renderer: function() {
							return '<img src="images/icons/arrow_right.png" title="' + tr.startLabel + '" alt="' + tr.start + '" />';
						},
						sortable: false,
						hideable: false,
						menuDisabled: true
					},
					{
						text: tr.stop,
						width: '60px',
						align: 'center',
						renderer:  function() {
							return '<img src="images/icons/cross.png" title="' + tr.stopLabel + '" alt="' + tr.stop + '" />';
						},
						sortable: false,
						hideable: false,
						menuDisabled: true
					}
				],
				store: CMDBuild.ServiceProxy.tasks.getStore()
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