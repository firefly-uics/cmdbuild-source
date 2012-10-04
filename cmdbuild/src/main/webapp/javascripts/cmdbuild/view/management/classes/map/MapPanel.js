Ext.define("CMDBuild.view.management.map.CMMapPanel", {
	extend: "Ext.panel.Panel",

	lon: undefined,
	lat: undefined,
	initialZoomLevel: undefined,

	initComponent : function() {
		this.hideMode = "offsets";
		this.cmAlreadyDisplayed = false;
		this.cmVisible = false;
		this.callParent(arguments);
	},

	// override
	onRender: function() {
		this.callParent(arguments);

		var map = CMDBuild.Management.MapBuilder.buildMap(this.body.dom.id),
			lon = this.lon || CMDBuild.Config.gis['center.lon'] || 0,
			lat = this.lat || CMDBuild.Config.gis['center.lat'] || 0,
			center = new OpenLayers.LonLat(lon,lat),
			projectedCenter = center.transform(new OpenLayers.Projection("EPSG:4326"),map.getProjectionObject()),
			initialZoomLevel = this.initialZoomLevel || CMDBuild.Config.gis.initialZoomLevel || 0;

		map.setCenter(projectedCenter, initialZoomLevel);

		this.getMap = function() {
			return map;
		};

		this.updateSize = function() {
			map.updateSize();
		};

		this.editingWindow = new CMDBuild.Management.MapEditingWindow({
			owner: this,
			map: map
		});

		this.mon(this.editingWindow.addButton, "toggle", function(button, toggled) {
			this.fireEvent("addFeatureButtonToogle", toggled);
		}, this);

		this.mon(this.editingWindow.removeButton, "click", function() {
			this.fireEvent("onRemoveFeatureButtonClick");
		}, this);

		this.mon(this.editingWindow, "cmGeoAttrMenuClicked", function(layer) {
			this.fireEvent("cmGeoAttrMenuClicked", layer);
		}, this);

		map.events.on({
			"addlayer": function(params) {
				this.fireEvent("addLayer", params);
			},
			"removelayer": function(params) {
				this.fireEvent("removeLayer", params);
			},
			scope: this
		});
	},

	setCmVisible: function(visible) {
		this.cmVisible = visible;
		this.fireEvent("cmVisible", visible);
		
		if (!this.cmAlreadyDisplayed) {
			var m = this.getMap();
			m.setCenter(m.center, m.zoom);
			this.cmAlreadyDisplayed = true;
		}
	},

	editMode: function() {
		if (this.editingWindow) {
			this.editingWindow.show();
		}
	},

	displayMode: function() {
		if (this.editingWindow) {
			this.editingWindow.hide();
		}
	},

	updateMap: function(entryType) {
		this.getMap().update(entryType, withEditLayer=true);
		this.editingWindow.update();
	}
});