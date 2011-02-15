(function() {
	CMDBuild.GeoUtils = {
		getGeoAttributes: function(classId) {
			//take the node of the class from the cache and look at the meta to find
			//geographical attributes
			var classTable = CMDBuild.Cache.getTableById(classId);
		  	if (classTable && classTable.meta) {
		  		return classTable.meta.geoAttributes || [];
		 	}
		  	return [];
		},
		readGeoJSON: function(geoJson) {
			var parser = new OpenLayers.Format.GeoJSON();
			return parser.parseGeometry(geoJson);
		}
	};
})();