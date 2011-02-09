(function() {
	var WMS_IMAGE_FORMAT = 'image/png';
    var GOESERVER_SERVICE_TYPE = "wms";
    var DEFAULT_MIN_ZOOM = 0;
    var DEFAULT_MAX_ZOOM = 25;
    
    var abstractLayer = {
    	activateStrategies: Ext.emptyFn,
	    selectFeatureByMasterCard: Ext.emptyFn,
	    selectFeature: Ext.emptyFn,
	    getFeatureByMasterCard: Ext.emptyFn,
	    clearSelection: Ext.emptyFn,
	    getEditedGeometry: Ext.emptyFn,
	    reselectLastSelection: Ext.emptyFn,
	    refreshFeatures: Ext.emptyFn,
	    setVisibilityByZoom: (function(zoom) {
			var isInRange = (zoom >= this.cmdb_minZoom && 
					zoom <= this.cmdb_maxZoom);
			this.setVisibility(isInRange);
		})
    };
    
	// say if an attribute belong to the passed table
	// or to an ancestor of him
	var isItMineOrOfMyAncestors = function(attr, tableId) {
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
	
	var buildEditLayer = function(geoAttribute) {		
		var onEditableLayerBeforeAdd = function(o) {
			if (editLayer.features.length > 0) {
				var currentFeature = editLayer.features[0];
				if (o.feature.attributes.master_card) {
					// we are added a feature in edit layer
					// because was selected by the user
					if (currentFeature.attributes.master_card == o.feature.attributes.master_card) {
						return false; // forbid the add
					} else {
						editLayer.removeFeatures([currentFeature]);
					}
				} else {
					// is added in editing mode
					// and want only one feature
					editLayer.removeAllFeatures();
					return true;
				}
			}
			return true;
		};
		
		var editLayer = new OpenLayers.Layer.Vector(geoAttribute.description+'-Edit', {
	    	projection: new OpenLayers.Projection("EPSG:900913"),
	    	displayInLayerSwitcher: false,
	    	eventListeners: {
				beforefeatureadded: onEditableLayerBeforeAdd
			},
			// cmdb stuff
            geoAttribute: geoAttribute
	    });
		return editLayer;
	};
	
	var buildCmdbLayer = function(geoAttribute, params, editLayer) {
		var layerDescription = geoAttribute.description;
		var onCmdbLayerBeforeAdd = function(o) {
			if (cmdbLayer.editLayer 
					&& o.feature.attributes.master_card == CMDBuild.State.getLastCardSelectedId()) {
				cmdbLayer.editLayer.addFeatures([o.feature]);
				cmdbLayer.lastSelection = o.feature.clone();			
				return false;
			}
		};
		
		var getIdClassForRequest = function(geoAttribute, tableId) {
			if (isItMineOrOfMyAncestors(geoAttribute, tableId)) {
				return tableId;
			} else {
				return geoAttribute.masterTableId;
			}
		};
		
		if (!editLayer) {
			// the layer belongs to another class
			var masterClass = CMDBuild.Cache.getTableById(geoAttribute.masterTableId);
			layerDescription = masterClass.text + " - " + layerDescription;
		}
		
		var cmdbLayer = new CMDBuild.Management.CMDBuildMap.MapLayer(layerDescription, {
			styleMap: new OpenLayers.StyleMap(Ext.decode(geoAttribute.style)),
	    	protocol: new OpenLayers.Protocol.HTTP({
	            url: 'services/json/gis/getgeocardlist',
	            params: {
	        		idClass: getIdClassForRequest(geoAttribute, params.classId),
	        		attribute: geoAttribute.name
	        	},
	            format: new OpenLayers.Format.GeoJSON()
	        }),
			eventListeners: {
				beforefeatureadded: onCmdbLayerBeforeAdd
			},
			
			// cmdb stuff
			cmdb_minZoom: geoAttribute.minZoom || DEFAULT_MIN_ZOOM,
			cmdb_maxZoom: geoAttribute.maxZoom || DEFAULT_MAX_ZOOM,
			geoAttribute: geoAttribute,
			editLayer: editLayer
		});
		
		return cmdbLayer;
	};
	
	var buildGeoserverLayer = function(geoAttribute, params, editLayer) {
		var geoserver_ws = CMDBuild.Config.gis.geoserver_workspace;
		var geoserver_url = CMDBuild.Config.gis.geoserver_url;
		var map = this;
		
		layer = new OpenLayers.Layer.WMS(
			geoAttribute.description,
			geoserver_url + "/" + GOESERVER_SERVICE_TYPE,
            {
                layers: geoserver_ws+":"+geoAttribute.name,
                format: WMS_IMAGE_FORMAT,
                transparent: true
            },{
            	singleTile: true,
            	ratio: 1
            }
        );
		
		layer.cmdb_minZoom = geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
		layer.cmdb_maxZoom = geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;
		layer.geoAttribute = geoAttribute;
		layer.editLayer = editLayer;		
		
		return layer;
	};
	
	CMDBuild.Management.CMDBuildMap.LayerBuilder = {
		buildLayer: function(params, geoAttribute) {
			if (!geoAttribute || !geoAttribute.isvisible
					|| !CMDBuild.Cache.getTableById(geoAttribute.masterTableId)) {
				return null;
			}

			var editLayer = null;
			var layer = null;
			
			if (isItMineOrOfMyAncestors(geoAttribute, params.classId)) {
				// add the edit layer only for the layer
				// defined for the current class or for an ancestor
				editLayer = buildEditLayer(geoAttribute);
			}
			
			if (geoAttribute.masterTableId) {
				layer = buildCmdbLayer(geoAttribute, params, editLayer);
			} else {
				layer = buildGeoserverLayer(geoAttribute, params, editLayer);
			}
			
			// complete the object with the not defined methods
			return Ext.applyIf(layer, abstractLayer);
		}
	};
})();