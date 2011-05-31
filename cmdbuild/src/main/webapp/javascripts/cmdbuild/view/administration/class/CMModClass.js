(function() {
	var tr = CMDBuild.Translation.administration.modClass;

	Ext.define("CMDBuild.view.administration.classes.CMModClass", {
		extend: "Ext.panel.Panel",

		NAME: "CMModClass",
		cmName:'class',

		constructor: function() {

			this.addClassButton = new Ext.button.Button({
				iconCls : 'add',
				text : tr.add_class
			});

			this.printSchema = new CMDBuild.PrintMenuButton({
				text : tr.print_schema,
				formatList: ['pdf', 'odt']
			});

			this.classForm = new CMDBuild.view.administration.classes.CMClassForm();

			this.domainGrid = new CMDBuild.Administration.DomainGrid({
				title : tr.tabs.domains
			});

			this.geoAttributesPanel = new CMDBuild.Administration.GeoAttributePanel({
				title: tr.tabs.geo_attributes
			});

			this.tabPanel = new Ext.tab.Panel({
				margin: "1 0 0 0",
				border: "0 0 0 0",
				frame: false,
				activeTab: 0,

				items: [{
					padding: "1 0 0 0",
					title: tr.tabs.properties,
					id: 'class_panel',
					layout: 'fit',
					items: [this.classForm]
				}
//	      		 ,{
//	        		title : tr.tabs.attributes,
//	        		disabled: true,
//	        		id : 'attr_panel',
//	        		layout : 'border',
//	        		items : [{
//			        	id: 'attributegrid',
//			        	xtype: 'attributegrid',
//			        	region: 'center',
//			        	style: {'border-bottom': '1px #D0D0D0 solid'}
//			    	},{
//			    		id: 'attributeform',
//						xtype: 'attributeform',
//						style: {'border-top': '1px #D0D0D0 solid'},
//						height: '50%',
//						region: 'south',
//						split:true
//	      		    }]
//	      		 }, 
				,this.geoAttributesPanel
//				,
//				new CMDBuild.Administration.LayerVisibilityGrid({
//					title: tr.layers,
//					withCheckToHideLayer: true,
//					disabled: true
//				}),
		      	 ,this.domainGrid
	      		 ]
	    	});

			Ext.apply(this, {
				tbar:[this.addClassButton, this.printSchema],
				title : tr.title,
				basetitle : tr.title+ ' - ',
				layout: 'fit',
				id : this.id + '_panel',
				items: [this.tabPanel],
				frame: false,
				border: false
			});

			this.callParent(arguments);
	  	},

		enableTabs: function(enable, idClass) {
//			var isStandard = true;
//			if (idClass && idClass > 0) {
//				var table = CMDBuild.Cache.getTableById(idClass);
//				isStandard	= table.tableType == "standard";
//			}
//			this.tabPanel.getItem('attr_panel').setDisabled(!enable);
//			this.tabPanel.getItem('dom_panel').setDisabled(!enable || !isStandard);
//			this.tabPanel.getItem('geo_layers').setDisabled((!enable || !isStandard) || !CMDBuild.Config.gis.enabled);	  		
//			this.tabPanel.getItem('geo_attr_panel').setDisabled((!enable || !isStandard) || !CMDBuild.Config.gis.enabled);
		},

		onSelectClass: function(table) {
			this.enableTabs(true, table.id);
		}

	});
})();