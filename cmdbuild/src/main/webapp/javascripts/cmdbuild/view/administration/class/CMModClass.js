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

			this.attributesPanel = new CMDBuild.view.administration.classes.CMClassAttributesPanel({
				title: tr.tabs.attributes,
				border: false
			})

			this.domainGrid = new CMDBuild.Administration.DomainGrid({
				title : tr.tabs.domains,
				border: false
			});

//			this.geoAttributesPanel = new CMDBuild.view.administration.classes.CMGeoAttributesPanel({
//				title: tr.tabs.geo_attributes
//			});

			this.tabPanel = new Ext.tab.Panel({
				frame: false,
				border: false,
				activeTab: 0,

				items: [{
					padding: "1 0 0 0",
					title: tr.tabs.properties,
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

				,this.attributesPanel
//				,this.geoAttributesPanel
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
				items: [this.tabPanel],
				frame: false,
				border: true
			});

			this.callParent(arguments);
		},
	
		onAddClassButtonClick: function() {
			this.tabPanel.setActiveTab(0);
		},

		onClassDeleted: function() {
			this.attributesPanel.disable();
//			this.geoAttributesPanel.disable();
//			this.domainGrid.disable();
		},

		onClassSelected: Ext.emptyFn
	});
})();