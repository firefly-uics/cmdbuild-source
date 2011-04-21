(function() {
	CMDBuild.Administration.ClassTree = Ext.extend(CMDBuild.TreePanel, {
		title: CMDBuild.Translation.administration.modClass.tree_title, 
		initComponent: function() {
			var tr = CMDBuild.Translation.common.tree_names;
			this.root = [
    		   CMDBuild.Cache.getTree("class", rootId=undefined, rootText=tr["class"], sorted = true),
    		   CMDBuild.Cache.getTree("simpletable", rootId="simpletable", rootText=tr.simpletable, sorted = true)
    		];
			this.rootVisible = false;
			this.border = false;
			this.fakeNodeEventName = "class";
			CMDBuild.Administration.ClassTree.superclass.initComponent.apply(this, arguments);
		}
	});

	CMDBuild.Administration.ModClass = Ext.extend(CMDBuild.ModPanel, {
		modtype:'class',	
		translation: CMDBuild.Translation.administration.modClass,
		initComponent : function() {

			this.addClassAction = new Ext.Action({
	        	iconCls : 'add',
	        	text : this.translation.add_class,
	        	handler : function() {
	        		this.tabPanel.setActiveTab('class_panel');
					this.enableTabs(false);
					this.publish("cmdb-addclassAction");
	      		},
	      		position: 'left',
	      		scope : this
	    	});
			
			this.printSchema = new CMDBuild.PrintMenuButton({      		
				text : this.translation.print_schema,
				callback: this.onPrintSchema,
				formatList: ['pdf', 'odt'],			
				scope: this
	    	});
	   
			this.tabPanel = new Ext.TabPanel({
	      		border : false,
	      		activeTab : 0,
	      		layoutOnTabChange : true,
	      		defaults : { 
	      			layout : 'fit'
	      		},
	      		items : [{
	       		 	title : this.translation.tabs.properties,
	        		id : 'class_panel',
	        		layout : 'fit',
	        		items :[{
			        	id: 'classform',
			        	xtype: 'classform',
			        	eventtype: this.modtype
			    	}]
	      		}, {
	        		title : this.translation.tabs.attributes,
	        		disabled: true,
	        		id : 'attr_panel',
	        		layout : 'border',
	        		items : [{
			        	id: 'attributegrid',
			        	xtype: 'attributegrid',
			        	region: 'center',
			        	style: {'border-bottom': '1px #D0D0D0 solid'}
			    	},{
			    		id: 'attributeform',
						xtype: 'attributeform',
						style: {'border-top': '1px #D0D0D0 solid'},
						height: '50%',
						region: 'south',
						split:true
	      		    }]
	      		 }, {
	      			xtype: 'geoattributepanel',
	      			id : 'geo_attr_panel',
	      			title: this.translation.tabs.geo_attributes,
	      			disabled: true
	      		 }, new CMDBuild.Administration.LayerVisibilityGrid({
	      			id : 'geo_layers',
	      			title: this.translation.layers,
	      			withCheckToHideLayer: true,
	      			disabled: true
		      	 }),
		      	 {
	        		title : this.translation.tabs.domains,
	        		disabled: true,
	        		id : 'dom_panel',
	        		xtype: 'domaingrid'
	      		}]
	    	});

	    	Ext.apply(this,{
	    		modtype: this.modtype,
				tbar:[this.addClassAction, this.printSchema],
	      		title : this.translation.title,
	      		basetitle : this.translation.title+ ' - ',
	      		layout: 'fit',
	      		id : this.id + '_panel',
	      		items: this.tabPanel
	    	});

	    	this.subscribe('cmdb-abortmodify-class', function enableTabs(p){
	    		this.enableTabs(p.idClass > 0, p.idClass);
	    	}, this);
	    	
	    	this.subscribe('cmdb-select-class', this.selectClass, this);
	    	this.subscribe('cmdb-deleted-class', function() {
	    		this.enableTabs(false);
	    		this.setTitle(this.basetitle);    		
	    	}, this);
	    	
	    	Ext.getCmp('attr_panel').on('activate', function(){
	    		Ext.getCmp('attributegrid').selectFirstRow();
	    	}, this);
	    	
			CMDBuild.Administration.ModClass.superclass.initComponent.apply(this, arguments);
	  	},
	  	
	  	enableTabs: function(enable, idClass) {
	  		var isStandard = true;
	  		if (idClass && idClass > 0) {
	  			var table = CMDBuild.Cache.getTableById(idClass);
	  			isStandard	= table.tableType == "standard";
	  		}
	  		this.tabPanel.getItem('attr_panel').setDisabled(!enable);
	  		this.tabPanel.getItem('dom_panel').setDisabled(!enable || !isStandard);
	  		this.tabPanel.getItem('geo_layers').setDisabled((!enable || !isStandard) || !CMDBuild.Config.gis.enabled);	  		
	        this.tabPanel.getItem('geo_attr_panel').setDisabled((!enable || !isStandard) || !CMDBuild.Config.gis.enabled);
	    },

	  	/**EVENT  FUNCTIONS **/
		  	
		/**
		 * @param {} params (optional) contains the selected class id and name
		 */
		selectClass: function(table) {
			if (table && table.id != "fakeNode") {
				this.publish('cmdb-init-class', {
						idClass: table.id,
						cachedNode: table
				});
				this.enableTabs(true, table.id);
			}		
	    },
	    
	    onPrintSchema: function(format) {
	    	CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url : 'services/json/schema/modreport/printschema',
				params: {format: format},
				method : 'POST',
				scope : this,
				success: function(response) {
					CMDBuild.LoadMask.get().hide();
					var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
						if (!popup) {
							CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
						}
				},
				failure: function(response) {			
					CMDBuild.LoadMask.get().hide();
				}
	  	 	});
	    }
	});
})();