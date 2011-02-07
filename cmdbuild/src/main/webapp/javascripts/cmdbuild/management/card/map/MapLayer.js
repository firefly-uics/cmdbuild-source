/**
 * @class CMDBuild.Management.CMDBuildMap.MapLayer
 */
CMDBuild.Management.CMDBuildMap.MapLayer = OpenLayers.Class(OpenLayers.Layer.Vector, {    
    initialize: function(name, options) {
		this.strategies = [
		    new OpenLayers.Strategy.BBOX({
		    	autoActivate: true
		    }),
		    new OpenLayers.Strategy.Refresh({
				autoActivate: true
			})		    
		];
		
		this.refreshFeatures = function() {
			var bboxStrategie = this.strategies[0];
			if (bboxStrategie.invalidBounds()) {
				bboxStrategie.calculateBounds();
			}
			bboxStrategie.triggerRead();
	    };
	    
        OpenLayers.Layer.Vector.prototype.initialize.apply(this, arguments);
    },    
    projection: new OpenLayers.Projection("EPSG:900913"),
	
    // CMDBuild stuff
    editLayer: undefined,
    geoAttribute: undefined,
   
	activateStrategies: function(activate) {
	    for (var i in this.strategies) {
	    	var s = this.strategies[i];
	    	// needed for method of the ext array called remove!
	    	if (activate) {
			    if (s.activate) { s.activate(); }
			    if (s.refresh) { s.refresh(); }
	    	} else {
	    		if (s.deactivate) { s.deactivate(); }
	    	}
	    }
	},
    
	selectFeatureByMasterCard: function(masterCardId) {
		var f = this.getFeatureByMasterCard(masterCardId);
		this.selectFeature(f);
    },
    
    selectFeature: function(f) {
    	if (f) {
			if (this.editLayer) {
				this.editLayer.removeAllFeatures();
				this.editLayer.addFeatures(f.clone());
			}
			this.lastSelection = f.clone();
		    this.removeFeatures([f]);
		    
		    // to reselect the feature after add or modify
		    if (this.lastSelection) {
			    this.addFeatures([this.lastSelection]);
		    }
		}
    },
    
    getFeatureByMasterCard: function(masterCard) {
	    var features = this.features;
	    for ( var i = 0, l = features.length; i < l; ++i) {
		    var f = features[i];
		    if (f.attributes.master_card == masterCard) {
			    return f;
		    }
	    }
	    return null;
    },
    
    clearSelection: function() {
	    if (this.lastSelection) {
		    this.addFeatures( [ this.lastSelection.clone() ]);
		    this.lastSelection = undefined;
	    }
	    if (this.editLayer) {
	    	this.editLayer.removeAllFeatures();
	    }
    },
    
    getEditedGeometry: function() {
    	try {
    		var f = this.editLayer.features;
    		return f[0].geometry;
    	} catch (Error) {
    		return null;
    	}
    },
 
    reselectLastSelection: function() {
    	if (this.lastSelection) {
    		this.selectFeature(this.lastSelection);
    	}
    }
});