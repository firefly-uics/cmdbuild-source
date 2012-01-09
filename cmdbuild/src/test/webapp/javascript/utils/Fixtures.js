_debug = function() {
	var prefix = "Debug";
	if (typeof arguments[0] == "string") {
		prefix += ": " + arguments[0];
	}
	console.debug(prefix, arguments);
};

Ext.define("CMDBuild.Config", {
	statics : {
		graph : {
			enabled : true
		},
		cmdbuild : {
			referencecombolimit : 500
		},
		gis: {
			enabled: false
		}
	}
});