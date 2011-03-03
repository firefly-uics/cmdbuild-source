(function() {
	CMDBuild.Management.CMDBuildMap.PopupController = OpenLayers.Class(OpenLayers.Control.SelectFeature, {
	    initialize: function(layers, options) {
			layers = layers || [];
			options = options || {};
			
			this.hover = true;
			this.highlightOnly = true;
			this.renderIntent = "temporary";
			this.overFeature = onFeatureOver;
			
			this.outFeature = function(f) {
				f.CM_over = false;
				f.layer.map.removeAllPopups();
			};
			
			OpenLayers.Control.SelectFeature.prototype.initialize.apply(this, [layers, options]);
		}
	});
	
	function onFeatureOver(f) {
		f.CM_over = true;
		
		function showInfoBaloon(f) {
			if (!f.CM_over || f.CM_busy) {
    			return;
    		}
    		
			if (f.CM_card) {
				buildPopUp(f);
			} else {
				f.CM_busy = true;
				CMDBuild.ServiceProxy.getCard(f.attributes.master_class, f.attributes.master_card, function(response, options, decoded) {
    				f.CM_busy = false;
    				f.CM_card = decoded.card;
    				f.CM_card_attributes = decoded.attributes;
    				buildPopUp(f);
    			});
			}
    	}
		
		Ext.defer(showInfoBaloon, 250, this, arguments);
		return true;
	}
	
	function buildPopUp(f) {
		var g = f.geometry;
		if (f.layer) {
			f.layer.map.addPopup(new OpenLayers.Popup.FramedCloud(
	            "cloud_"+f.id, 
	            new OpenLayers.LonLat(g.x, g.y),
	            null,
	            buildPopupContent(f),
	            null,
	            closeButton = false
	        ), exlusive=true);
		}
	}
	
	function buildPopupContent(f) {
		var card = f.CM_card;
		var attributes = f.CM_card_attributes;
		
		var htmlTemplate ="<div class=\"map_cloud_content\">{0}</div>";
		var itemTemplate = "<p class=\"map_cloud_item\"><strong>{0}:</strong>"
			+ "<span>{1}</span></p>";
		
		
		var items = "";
		for (var i=0, l=attributes.length; i<l; ++i) {
			var at = attributes[i];
			if (at.isbasedsp) {
				var attrValue = card[at.name+"_value"] || card[at.name] || "-";
				items += String.format(itemTemplate, at.description, attrValue);
			}
		}
		
		return String.format(htmlTemplate, items);
	}
})();
