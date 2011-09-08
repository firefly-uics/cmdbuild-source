(function() {
	CMDBuild.GeoUtils = {
		getGeoAttributes: function(classId) {
			// take the node of the class from the cache and look at the meta to find
			// geographical attributes
			var et = _CMCache.getEntryTypeById(classId);
			if (et) {
				return et.getGeoAttrs();
			}

			return [];
		},

		readGeoJSON: function(geoJson) {
			var parser = new OpenLayers.Format.GeoJSON();
			return parser.parseGeometry(geoJson);
		}
	};
})();