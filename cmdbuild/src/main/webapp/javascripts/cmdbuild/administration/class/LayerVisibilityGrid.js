(function() {
	var selectVisibleLayers = function(tableId) {
		var table = CMDBuild.Cache.getTableById(tableId);
		var geoAttrs = [];
		if (table) {
		 geoAttrs = table.meta.geoAttributes;
		}
		
		var s = this.store;
		var di = this.getVisibilityColDataIndex();
		
		var geoAttrEquals = function(a1, a2) {
			if (a1.masterTableId) {
				return a1.masterTableId == a2.masterTableId && a1.name == a2.name;					
			} else {
				// geoserver layer
				return !a2.masterTableId && a1.name == a2.name;
			}
		};
		
		s.each(function(record) {
			var visible = false;
			for (var i=0, l=geoAttrs.length; i<l; ++i) {
				var attr = geoAttrs[i];
				if (geoAttrEquals(attr, record.data)) {
					visible = attr.isvisible;
					break;
				}
			}
			record.set(di, visible);
		}, this);
	};
	
	Ext.define("CMDBuild.Administration.LayerVisibilityGrid", {
		extend: "CMDBuild.Administration.LayerGrid",
		currentClass: undefined,
		initComponent: function() {
			CMDBuild.Administration.LayerVisibilityGrid.superclass.initComponent.call(this, arguments);
			
			this.store.on("load", function() {
				selectVisibleLayers.call(this, this.currentClass);
			}, this);
			
			this.subscribe("cmdb-select-class", function(p) {
				this.currentClass = p.id;
				selectVisibleLayers.call(this, p.id);
			}, this);
		},
		/**
		 * @override
		 */
		onVisibilityChecked: function(event, element, column, record) {
		    var checked = record.data[column.dataIndex];
		    var classId = this.currentClass;
		    var loadMask = CMDBuild.LoadMask.get();
		    
		    var onSuccess = (function() {
		    	this.publish("cmdb-changevisibility-geoattr", {
		    		checked: checked,
		    		classId: classId,
		    		record: record
		    	});
		    }).createDelegate(this);
		    
		    loadMask.show();
		    CMDBuild.ServiceProxy.saveLayerVisibility({
		    	classId: classId,
				master: record.data.masterTableId,
				featureTypeName: record.data.name,
				checked: checked,
				success: onSuccess,
				failure: function() {
		    		record.set(column.dataIndex, !checked);
		    	},
				callback: function() {
		    		loadMask.hide();
		    	}
		    });
		}
	});
})();