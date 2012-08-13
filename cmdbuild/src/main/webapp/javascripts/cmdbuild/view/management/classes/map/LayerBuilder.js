(function() {
	var WMS_IMAGE_FORMAT = 'image/png',
		GOESERVER_SERVICE_TYPE = "wms",
		DEFAULT_MIN_ZOOM = 0,
		DEFAULT_MAX_ZOOM = 25;

	function AbstractLayer() {};

	AbstractLayer.prototype = {
		CMDBuildLayer : true,
		CM_Layer : true,
		activateStrategies : Ext.emptyFn,
		selectFeatureByMasterCard : Ext.emptyFn,
		selectFeature : Ext.emptyFn,
		getFeatureByMasterCard : Ext.emptyFn,
		clearSelection : Ext.emptyFn,
		getEditedGeometry : Ext.emptyFn,
		reselectLastSelection : Ext.emptyFn,
		refreshFeatures : Ext.emptyFn,
		setVisibilityByZoom : (function(zoom) {
			var isInRange = (zoom >= this.cmdb_minZoom && zoom <= this.cmdb_maxZoom);
			this.setVisibility(isInRange);
		})
	};

	CMDBuild.Management.CMMap.LayerBuilder = {
		/*
		 * buildLayer configuration object:
    	 * {
    	 * 	classId: integer,
    	 * 	geoAttribute: a cached attribute of the class referred from classId
    	 * 	withEditLayer: boolean to say if we want a editLayer or not 
    	 * }
    	 */
		buildLayer: function(config) {
			var classId = config.classId,
				geoAttribute = config.geoAttribute,
				withEditLayer = config.withEditLayer,
				editLayer = null,
				layer = null;

			if (!geoAttribute || !geoAttribute.isvisible) {
				return null;
			}

			if (isItMineOrOfMyAncestors(geoAttribute, classId) 
					&& withEditLayer) {
				// add the edit layer only for the layer
				// defined for the current class or for an ancestor
				editLayer = buildEditLayer(geoAttribute);
			}

			if (geoAttribute.masterTableId) {
				layer = buildCmdbLayer(geoAttribute, classId, editLayer);
			} else {
				layer = buildGeoserverLayer(geoAttribute);
			}

			return layer;
		}
	};

	function buildCmdbLayer(geoAttribute, classId, editLayer) {
		var layerDescription = geoAttribute.description;

		if (!editLayer) {
			// the layer belongs to another class
			var masterClass = _CMCache.getEntryTypeById(geoAttribute.masterTableId);
			if (masterClass) {
				layerDescription = masterClass.get("text") + " - " + layerDescription;
			}
		}

		var layer = new CMDBuild.Management.CMMap.MapLayer(layerDescription, {
			targetClassId: getIdClassForRequest(geoAttribute, classId),
			geoAttribute: geoAttribute,
			editLayer: editLayer
		});

		return Ext.applyIf(layer, new AbstractLayer());
	};

	function buildEditLayer(geoAttribute) {
		// the edit layer is used to manage the single insert.
		// maybe it's possible to do the same using the cmdblayer only, 
		// and a "edit mode" in which consider the single insert
		var editLayer = new OpenLayers.Layer.Vector(geoAttribute.description+'-Edit', {
			projection: new OpenLayers.Projection("EPSG:900913"),
			displayInLayerSwitcher: false,

			// cmdb stuff
			geoAttribute: geoAttribute,
			CM_EditLayer: true,
			CM_Layer: true
		});

		return editLayer;
	};

	function buildGeoserverLayer(geoAttribute) {
		var geoserver_ws = CMDBuild.Config.gis.geoserver_workspace,
			geoserver_url = CMDBuild.Config.gis.geoserver_url;

		var layer = new OpenLayers.Layer.WMS(geoAttribute.description,
				geoserver_url + "/" + GOESERVER_SERVICE_TYPE, {
					layers : geoserver_ws + ":" + geoAttribute.name,
					format : WMS_IMAGE_FORMAT,
					transparent : true
				}, {
					singleTile : true,
					ratio : 1
				});

		layer.cmdb_minZoom = geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
		layer.cmdb_maxZoom = geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;
		layer.geoAttribute = geoAttribute;
		layer.editLayer = undefined;

		layer = Ext.applyIf(layer, new AbstractLayer());

		layer.CMDBuildLayer = false;
		layer.CM_Layer = false;
		return layer;
	};

	// say if an attribute belong to the passed table
	// or to an ancestor of him
	function isItMineOrOfMyAncestors(attr, tableId) {
		var table = _CMCache.getEntryTypeById(tableId);

		while (table) {
			if (attr.masterTableId == table.get("id")) {
				return true;
			} else {
				var parentId = table.get("parent");
				if (parentId) {
					table = _CMCache.getEntryTypeById(parentId);
				} else {
					table = null;
				}
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