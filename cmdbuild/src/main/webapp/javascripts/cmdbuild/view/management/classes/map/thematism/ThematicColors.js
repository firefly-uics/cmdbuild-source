(function() {
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicColors', {
		getColor : function(value, colorsTable) {
			var color = getColorFromTable(value, colorsTable);
			if (color) {
				return color;
			}
			if (value === "true")
				return "#9999FF";
			if (value === "false")
				return "#FF0000";
			var red = value;
			return rgbToHex(parseInt(red), 0, 0);
		}
	});

	function componentToHex(c) {
		var hex = c.toString(16);
		return hex.length == 1 ? "0" + hex : hex;
	}
	function rgbToHex(r, g, b) {
		return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
	}
	function getColorFromTable(value, colorsTable) {
		for (var i = 0; colorsTable && i < colorsTable.length; i++) {
			if (sameValue(colorsTable[i].value, value)) {
				return colorsTable[i].color;
			}
		}
		return null;
	}
	function sameValue(first, second) {
		return (first === second || 
				(first === "true" && second === true) || 
				(first === "false" && second === false) || 
				(first === "null" && second === null)|| 
				(first === null && second === "null")|| 
				parseInt(first) === parseInt(second));

	}
})();