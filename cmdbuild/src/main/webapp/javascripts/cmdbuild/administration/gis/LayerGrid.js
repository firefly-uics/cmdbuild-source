(function() {
	var tr = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geo = CMDBuild.Translation.administration.modClass.geo_attributes;
	
	var buildCheckColumn = function() {
		var column = new Ext.grid.CheckColumn({
	        header: tr_geo.visibility,
	        dataIndex: "isvisible"
		});
		
		var grid = this;
		column.onMouseDown = function(e,t) {
		    if (t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
		        e.stopEvent();
		        var index = grid.getView().findRowIndex(t);
		        var recordToChange = grid.store.getAt(index);
		        //this set the checked box to true
		        recordToChange.set(this.dataIndex, !recordToChange.data[this.dataIndex]);
		        
		        grid.onVisibilityChecked(e, t, column, recordToChange);
		    }
		};
		
		this.plugins = [column];
		this.getVisibilityColDataIndex = function() {
			return column.dataIndex;
		};
		return column;
	};
	
	var ddRender = function(g) {
	    var ddrow = new Ext.ux.dd.GridReorderDropTarget(g, {
            copy: false,
            listeners: {
                beforerowmove: g.beforeRowMove
	    	}
        });

	    Ext.dd.ScrollManager.register(g.getView().getEditorParent());
    };
    
	CMDBuild.Administration.LayerGrid = Ext.extend(Ext.grid.GridPanel, {
		region: 'center',
		frame: false,
	    border: false,
		store: CMDBuild.ServiceProxy.getAllLayerStore(),
		ddGroup : 'layersGridDDGroup',
		// custom stuff
		withCheckToHideLayer: false,
		viewConfig: {
	        forceFit: true
	    },
	    sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
		initComponent: function() {
	    	var columns = [{
	            header: tr.description,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "description"
	        },{
	        	header: tr_geo.master,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "masterTableName"
	        },{
	            header: tr.type,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "type"
	        },{
	            header: tr_geo.min_zoom,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "minZoom"
	        },{
	            header: tr_geo.max_zoom,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "maxZoom"
	        }];
	    	
	    	if (this.withCheckToHideLayer) {    		
	    		columns.push(buildCheckColumn.call(this));
	    	}
	    	
	    	this.colModel = new Ext.grid.ColumnModel( {
		        defaults: {
		            width: 120,
		            sortable: true
		        },	                
		        columns: columns
		    });
	    	
			CMDBuild.Administration.LayerGrid.superclass.initComponent.call(this, arguments);
			
			this.on({
				render: ddRender
			});
		},
		
		/**
		 * template method for the subclasses
		 */
		beforeRowMove: function(objThis, oldIndex, newIndex, records) {
            return true;
        },
		
		/**
		 * template method for the subclasses
		 */
		onVisibilityChecked: function(event, element, column, record) {}
	});
})();