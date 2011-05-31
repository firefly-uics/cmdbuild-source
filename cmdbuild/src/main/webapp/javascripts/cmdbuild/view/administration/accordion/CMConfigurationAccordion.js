(function() {

	var tr = CMDBuild.Translation.administration.setup;
	
	var store = Ext.create('Ext.data.TreeStore', {
		fields: [
			{name: 'cmName', type: 'string'},
			{name: 'text', type: 'string'}
		],
		root : {
			expanded : true,
			children : [{
				text: tr.cmdbuild.menuTitle,
				leaf : true,
				cmName: "modsetupcmdbuild"
			},{
				text: tr.workflow.menuTitle,
				leaf : true,
				cmName: 'modsetupworkflow'
			},{
				text: tr.workflow.email.title,
				leaf : true,
				cmName: 'modsetupemail'
			},{
				text: tr.graph.menuTitle,
				leaf : true,
				cmName: 'modsetupgraph'
			},{
				text: tr.legacydms.menuTitle, 
				leaf : true,
				cmName: 'modsetupalfresco'
			},{
				text: tr.gis.title, 
				leaf : true,
				cmName: 'modsetupgis'
			},{
				text: tr.server.menuTitle, 
				leaf: true,
				cmName: 'modsetupserver'
			}]
		}
	});

	Ext.define("CMDBuild.view.administraton.accordion.CMConfigurationAccordion", {
		extend: 'Ext.panel.Panel',
		
		cmName: "setup",
		hideMode: "offsets",
		constructor: function() {
			this.tree = Ext.create("Ext.tree.Panel", {
				store: store,
				border: false,
				region: "center",
				rootVisible: false
			});
			
			Ext.apply(this, {
				title: tr.setupTitle,
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