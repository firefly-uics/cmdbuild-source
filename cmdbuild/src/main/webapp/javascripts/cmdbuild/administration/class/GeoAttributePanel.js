(function(){
	var tr_attributes = CMDBuild.Translation.administration.modClass.attributeProperties
	var gridColumns =  [{
			header: tr_attributes.type,
	        sortable: true,
	        dataIndex: 'type'
	    },{
	        header: tr_attributes.name,
	        sortable: true,
	        dataIndex: 'name'
	    },{
	        header: tr_attributes.description,
	        sortable: true,
	        dataIndex: 'description'
	    }];
	
	var isItMineOrOfMyParents = function(attr, tableId) {
		var table = CMDBuild.Cache.getTableById(tableId);
		while (table) {
			if (attr.masterTableId == table.id) {
				return true;
			} else {
				table = CMDBuild.Cache.getTableById(table.parent);
			}
		}
		return false;
	};
	
	CMDBuild.Administration.GeoAttributePanel = Ext.extend(Ext.Panel, {
	    eventtype: "class",
	    initComponent: function() {
		    var onAdd = function() {
		    	grid.getSelectionModel().clearSelections();
		    	form.prepareToAdd();
		    };
		    
		    var onRowSelect = function(sm, index, record) {
		    	form.loadRecordAfterGridSelection(record);
		    };
		    
		    var gridStore = new Ext.data.SimpleStore( {
		        fields: ["name", "type", "description", "minzoom",
		                 "maxzoom", "style", "masterTableId"]
		    });
	
		    var grid = new Ext.grid.GridPanel( {
		        store: gridStore,
		        height: 100,
		        split: true,
		        viewConfig: {
			        forceFit: true
		        },
		        region: "north",
		        columns: gridColumns,
		        stripeRows: true
		    });
		    
		    var form = new CMDBuild.Administration.GeoAttributeForm({
		    	region: "center",
	            layout: "fit"	
		    });
		    
		    Ext.apply(this, {
		        frame: false,
		        layout: "border",
		        tbar: [ {
		            text: tr_attributes.add_attribute,
		            iconCls: "add",
		            handler: onAdd
		        } ],
		        items: [ grid, form ]
		    });
		    
		    var selectClass = function(o) {
		    	var tableId = o.id;
		    	if (o.meta) {
		    		var geoAttributes = o.meta.geoAttributes || [];
		    		gridStore.removeAll();
		    		for (var i=0,l=geoAttributes.length; i<l; ++i) {
		    			var attr = geoAttributes[i];
		    			if (isItMineOrOfMyParents(attr, tableId)) {
		    				var r = new gridStore.recordType(attr);
		    				gridStore.insert(0, r);
		    			}
		    		}		    		
		    	}
		    	form.setClass(tableId);
		    };
		    
		    CMDBuild.Administration.GeoAttributePanel.superclass.initComponent.apply(this, arguments);
		    grid.getSelectionModel().on("rowselect", onRowSelect , this);
		    this.subscribe('cmdb-select-class', selectClass, this);
	    }	    	 
	});
	Ext.reg("geoattributepanel", CMDBuild.Administration.GeoAttributePanel);
})();