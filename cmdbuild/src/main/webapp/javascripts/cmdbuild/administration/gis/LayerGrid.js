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
    
	Ext.define("CMDBuild.Administration.LayerGrid", {
		extend: "Ext.grid.Panel",
		region: 'center',
		frame: false,
	    border: false,
		store: CMDBuild.ServiceProxy.getAllLayerStore(),
		ddGroup : 'layersGridDDGroup',
		// custom stuff
		withCheckToHideLayer: false,
		
		sm: new Ext.selection.RowModel(),
		initComponent: function() {
	    	this.columns = [{
	            header: tr.description,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "description",
	            flex: 1
	        },{
	        	header: tr_geo.master,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "masterTableName",
	            flex: 1
	        },{
	            header: tr.type,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "type",
	            flex: 1
	        },{
	            header: tr_geo.min_zoom,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "minZoom",
	            flex: 1
	        },{
	            header: tr_geo.max_zoom,
	            hideable: true,
	            hidden: false,
	            sortable: false,
	            dataIndex: "maxZoom",
	            flex: 1
	        }];
	    	
	    	if (this.withCheckToHideLayer) {    		
	    		columns.push(buildCheckColumn.call(this));
	    	}
	    	
			this.callParent(arguments);
			
//			this.on({
//				render: ddRender
//			});
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