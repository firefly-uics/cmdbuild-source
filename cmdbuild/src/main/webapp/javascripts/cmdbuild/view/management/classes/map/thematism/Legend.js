(function() {

	Ext.define('CMDBuild.view.management.classes.map.thematism.Legend', {
		extends : [ Ext.Object ],
		parentDiv : undefined,
		constructor : function(parameters) {
			this.parentDiv = parameters.parentDiv
			var div = "<div id='CMDBUILD_THEMATHICLEGEND' style='position: absolute;'></div>";
			this.parentDiv.innerHTML = div;
		},
		compose : function() {
			this.grid = Ext.create("CMDBuild.view.management.classes.map.thematism.LegendPanel", {
				renderTo : "CMDBUILD_THEMATHICLEGEND",
				value : 100,
				listeners : {}
			});
		},
		refreshResults : function(layers) {
			this.grid.show();
			this.grid.refreshResults(layers);
		},
		hide : function() {
			this.grid.hide();
		}
	});
	Ext.define("CMDBuild.view.management.classes.map.thematism.LegendPanel", {
		extend : "Ext.panel.Panel",
		width : 200,
		height : 150,
		layout : 'fit',
		resizable : true,
		initComponent : function() {
			var me = this;
			// this panel only for have scroolbars on the Grid
			this.createControls();
			Ext.apply(this, {
				items : [ this.grid ],
				hidden: true,
			});
			this.callParent(arguments);
		},
		createControls : function() {
			this.colorsStore = Ext.create("Ext.data.Store", {
				fields : [ "value", "cardinality", "color" ],
				data : []
			});
			var colorPicker = Ext.create('CMDBuild.view.common.field.picker.Color');
			this.grid = Ext.create("Ext.grid.Panel", {
				title : "Results",
				store : this.colorsStore,
				width : "100%",
				height : "100%",
				plugins : [ Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit : 1
				}) ],
				columns : [ {
					text : "@@ Value",
					dataIndex : "value",
					flex : 1
				}, {
					text : "Cardinality",
					dataIndex : "cardinality"
				}, {
					text : "@@ Color",
					dataIndex : "color",
					renderer : function(value, metaData) {
						metaData.style = "background-color:" + value + ";";
						return value;
					},
					editor : colorPicker
				} ],
				height : "100%",
				width : "100%"
			});
		},
		chargeStore : function(cardsStore, colorsTable) {
			for (var i = 0; i < colorsTable.length; i++) {
				cardsStore.add({
					value : colorsTable[i].value,
					cardinality : colorsTable[i].cardinality,
					color : colorsTable[i].color
				});
			}
		},
		refreshResults : function(layers) {
			var currentResultsStore = Ext.create("Ext.data.Store", {
				fields : [ "value", "cardinality", "color" ],
				autoLoad : false,
				data : []
			});
			var colorsTable = layers[0].get("adapter").getColorsTable();//N.B.
			this.chargeStore(currentResultsStore, colorsTable);
			this.grid.getStore().loadData(currentResultsStore.getRange(), false);
		}
	});

})();