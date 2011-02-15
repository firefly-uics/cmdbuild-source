(function() {
	var WMS_IMAGE_FORMAT = 'image/png';
    var GOESERVER_SERVICE_TYPE = "wms";
    var DEFAULT_MIN_ZOOM = 0;
    var DEFAULT_MAX_ZOOM = 25;
    
    var AbstractLayer = function () {};
    AbstractLayer.prototype = {
    	CMDBuildLayer: true,
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
    
    CMDBuild.Management.CMDBuildMap.LayerBuilder = {
    	/*
    	 * buildLayer configuration object:
    	 * {
    	 * 	classId: integer,
    	 * 	geoAttribute: a cached attribute of the class referred from classId
    	 * 	withEditLayer: boolean to say if we want a editLayer or not 
    	 * }
    	 */
		buildLayer: function(config) {
    		var classId = config.classId;
    		var geoAttribute = config.geoAttribute;
    		var withEditLayer = config.withEditLayer; 
    		
			if (!geoAttribute || !geoAttribute.isvisible
					|| !CMDBuild.Cache.getTableById(geoAttribute.masterTableId)) {
				return null;
			}
		
			var editLayer = null;
			var layer = null;
			
			if (isItMineOrOfMyAncestors(geoAttribute, classId) 
					&& withEditLayer) {
				// add the edit layer only for the layer
				// defined for the current class or for an ancestor
				editLayer = buildEditLayer(geoAttribute);
			}
			
			if (geoAttribute.masterTableId) {
				layer = buildCmdbLayer(geoAttribute, classId, editLayer);
			} else {
				layer = buildGeoserverLayer(geoAttribute, editLayer);
			}
			
			// complete the object with the not defined methods
			return Ext.applyIf(layer, new AbstractLayer());
		}
	};
	
	function buildEditLayer(geoAttribute) {
		// the edit layer is used to manage the single insert.
		// maybe it's possible to do the same using the cmdblayer only, 
		// and a "edit mode" in which consider the single insert
		_debug("Edit Layer", geoAttribute);
		var editLayer = new OpenLayers.Layer.Vector(geoAttribute.description+'-Edit', {
	    	projection: new OpenLayers.Projection("EPSG:900913"),
	    	displayInLayerSwitcher: false,
	    	eventListeners: {
				beforefeatureadded: onEditableLayerBeforeAdd
			},
			// cmdb stuff
            geoAttribute: geoAttribute
	    });
		
		function onEditableLayerBeforeAdd(o) {
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
		
		return editLayer;
	};
	
	function buildCmdbLayer(geoAttribute, classId, editLayer) {
		_debug("CMDB Layer", geoAttribute);
		var layerDescription = geoAttribute.description;
		
		if (!editLayer) {
			// the layer belongs to another class
			var masterClass = CMDBuild.Cache.getTableById(geoAttribute.masterTableId);
			layerDescription = masterClass.text + " - " + layerDescription;
		}
		
		var cmdbLayer = new CMDBuild.Management.CMDBuildMap.MapLayer(layerDescription, {
			eventListeners: {
				beforefeatureadded: function onCmdbLayerBeforeAdd(o) {
					if (cmdbLayer.editLayer 
							&& o.feature.attributes.master_card == CMDBuild.State.getLastCardSelectedId()) {
						cmdbLayer.editLayer.addFeatures([o.feature]);
						cmdbLayer.lastSelection = o.feature.clone();			
						return false;
					}
				}
			},
			targetClassId: getIdClassForRequest(geoAttribute, classId),
			geoAttribute: geoAttribute,
			editLayer: editLayer
		});
		
		return cmdbLayer;
	};
	
	function buildGeoserverLayer(geoAttribute, editLayer) {
		var geoserver_ws = CMDBuild.Config.gis.geoserver_workspace;
		var geoserver_url = CMDBuild.Config.gis.geoserver_url;
		var map = this;
		
		var layer = new OpenLayers.Layer.WMS(
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
	
	
	// say if an attribute belong to the passed table
	// or to an ancestor of him
	function isItMineOrOfMyAncestors(attr, tableId) {
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
	
	function getIdClassForRequest(geoAttribute, tableId) {
		if (isItMineOrOfMyAncestors(geoAttribute, tableId)) {
			return tableId;
		} else {
			return geoAttribute.masterTableId;
		}
	};
	
})();