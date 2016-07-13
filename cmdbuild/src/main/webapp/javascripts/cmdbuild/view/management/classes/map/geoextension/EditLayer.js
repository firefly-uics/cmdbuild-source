/*(function() {
	Ext.define('CMDBuild.view.management.classes.map.geoextension.EditLayer', {
		constructor : function(classId, geoAttribute, withEditWindow) {
			this.layer = oPrivate.buildEditLayer(geoAttribute);
			this.layer.set("name",  "cmdbuildEditLayer");
			this.callParent(arguments);
		},
		getLayer : function() {
			return this.layer;
		},
		getSource : function() {
			return this.layer.getSource();
		}
	});
	var oPrivate = {
			buildEditLayer : function(geoAttribute) {
				var view = new ol.View({
					projection: "EPSG:900913"
				});
				var editLayer = new ol.layer.Vector(name, {
					view: view,
					// cmdb stuff
					geoAttribute: geoAttribute,
					CM_EditLayer: true,
					CM_Layer: true
				});
				return editLayer;
			}
		};

})();*/
