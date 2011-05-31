(function() {

	var tr = CMDBuild.Translation.administration.modcartography;
	
	var store = Ext.create('Ext.data.TreeStore', {
		fields: [
			{name: 'cmName', type: 'string'},
			{name: 'text', type: 'string'}
		],
		root : {
			expanded : true,
			children : [{
				text: tr.icons.title,
				leaf : true,
				cmName: "gis-icons"
			},
			{
				text: tr.external_services.title,
				leaf : true,
				cmName: "gis-external-services"
			},
			{
				text: tr.layermanager.title,
				leaf : true,
				cmName: "gis-layers-order"
			},
			{
				text: tr.geoserver.title,
				leaf : true,
				cmName: "gis-geoserver"
			}
			]
		}
	});

	Ext.define("CMDBuild.view.administraton.accordion.CMGISAccordion", {
		extend: 'Ext.panel.Panel',
		constructor: function() {
			this.tree = Ext.create("Ext.tree.Panel", {
				store: store,
				border: false,
				region: "center",
				rootVisible: false
			});
			
			Ext.apply(this, {
				title: tr.title,
				items: [this.tree],
				layout: "border",
				border: false
			});
			
			this.callParent(arguments);
		},

		getSelectionModel: function() {
			return this.tree.getSelectionModel();
		},

		getRootNode: function() {
			return this.tree.getRootNode();
		}
	});

})();