(function(){
	var tr_attributes = CMDBuild.Translation.administration.modClass.attributeProperties
	
	Ext.define("CMDBuild.Administration.GeoAttributePanel", {
		extend: "Ext.Panel",
		alias: "geoattributepanel",

		constructor: function() {
			this.addButton = new Ext.button.Button({
				text: tr_attributes.add_attribute,
				iconCls: "add"
			} );

			this.grid = new Ext.grid.GridPanel( {
				store: new Ext.data.SimpleStore( {
					model: "GISLayerModel"
				}),
				height: 100,
				split: true,
				region: "north",
				stripeRows: true,
				columns: [{
					header: tr_attributes.type,
					sortable: true,
					dataIndex: 'type',
					flex: 1
				},{
					header: tr_attributes.name,
					sortable: true,
					dataIndex: 'name',
					flex: 1
				},{
					header: tr_attributes.description,
					sortable: true,
					dataIndex: 'description',
					flex: 1
				}]
			});

			this.form = new CMDBuild.Administration.GeoAttributeForm({
				region: "center"
			});

			Ext.apply(this, {
				frame: false,
				layout: "border",
				tbar: [this.addButton],
				items: [this.form, this.grid]
			});

			this.callParent(arguments);
		}
	});
})();