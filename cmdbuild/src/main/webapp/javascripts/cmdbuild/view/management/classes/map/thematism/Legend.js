(function() {

	Ext.define('CMDBuild.view.management.classes.map.thematism.Legend', {
		extends : [Ext.Object],
		parentDiv : undefined,
		constructor : function(parameters) {
			this.parentDiv = parameters.parentDiv
			var div = "<div id='CMDBUILD_THEMATHICLEGEND' style='position: absolute;'></div>";
			this.parentDiv.innerHTML = div;
		},
		compose : function() {
	          var slider = new Ext.Slider({
	                renderTo: "CMDBUILD_THEMATHICLEGEND",
	                value: 100,
	                listeners: {
	                }
	            });
		}
	});
})();