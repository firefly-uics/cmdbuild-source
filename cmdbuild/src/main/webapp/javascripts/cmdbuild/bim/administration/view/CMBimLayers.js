(function() {

	Ext.define("CMDBuild.bim.administration.view.CMBimLayersDelegate", {
		/**
		 * Called after a click on
		 * a check column
		 * 
		 * @param {CMDBuild.bim.administration.view.CMBimLayers} grid the grid
		 * that call this method
		 * @param {CMDBuild.bim.data.CMBimLayerModel} record the record
		 * that holds the data of the clicked row
		 * @param {String} dataIndex the name of the clicked column
		 * @param {boolean} checked the new value of the check column
		 */
		onCheckColumnChange: function(grid, record, dataIndex, checked) {}
	});

	Ext.define("CMDBuild.bim.administration.view.CMBimLayers", {
		extend: "Ext.panel.Panel",

		delegate: new CMDBuild.bim.administration.view.CMBimLayersDelegate(),

		initComponent: function() {
			var me = this;

			this.store = CMDBuild.bim.proxy.layerStore();
			var grid = Ext.create('Ext.grid.Panel', {
				title: CMDBuild.Translation.bim + " " + CMDBuild.Translation.layers,
				region: 'center',
				store: this.store,
				columns: [
					{
						flex: 1,
						text: CMDBuild.Translation.class,
						dataIndex: 'description'
					},
					checkColumn(me, CMDBuild.Translation.active, "active"),
					checkColumn(me, CMDBuild.Translation.root, "root"),
					checkColumn(me, CMDBuild.Translation.export, "export"),
					checkColumn(me, CMDBuild.Translation.container, "container")
				]
			});

			this.layout = 'border';
			this.items = [grid];
			this.callParent(arguments);
		},

		load: function() {
			this.store.load();
		},

		onCheckColumnChange: function(cell, recordIndex, checked) {
			var dataIndex = cell.dataIndex;
			var record = this.store.getAt(recordIndex);

			this.delegate.onCheckColumnChange(this, record, dataIndex, checked);
		}
	});

	function checkColumn(me, header, dataIndex) {
		return {
			align: "center",
			dataIndex: dataIndex,
			fixed: true,
			text: header,
			width: 70,
			xtype: 'checkcolumn',
			listeners: {
				checkchange: function(cell, recordIndex, checked) {
					me.onCheckColumnChange(cell, recordIndex, checked);
				}
			}
		};
	}

})();