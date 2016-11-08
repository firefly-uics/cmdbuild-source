(function() {

	Ext.require('CMDBuild.proxy.gis.Gis');

	var GEOSERVER = "GeoServer";

	Ext.define("CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource", {
		GEOSERVER: GEOSERVER,
		constructor: function(navigationPanel, mapState) {
			this.navigationPanel = navigationPanel;
			this.mapState = mapState;
			this.configuration = CMDBuild.configuration.gis.get('cardBrowserByDomainConfiguration'); // TODO: use proxy constants
			this.refresh();
			this.callParent(arguments);
		},

		refresh: function() {
			var me = this;
			me.navigationPanel.setRootNode({
				loading: true,
				text: CMDBuild.Translation.common.loading
			});

			// fill the first level of tree nodes
			// asking the cards according to the
			// root of the configuration
			CMDBuild.proxy.gis.Gis.expandDomainTree({
				loadMask: false,
				success: function successGetCardBasicInfoList(operation, options, response) {
					addGeoserverLayersToTree(response.root, me);
					me.navigationPanel.setRootNode(response.root);
					me.navigationPanel.loaded();
				}
			});
		}
	});

	function addGeoserverLayersToTree(root, me) {
		var children = (root) ? root.children || [] : [];
		for (var i=0, l=children.length; i<l; ++i) {
			addGeoserverLayersToTree(children[i], me);
		}

		addGeoserverLayerIfConfigured(root, me);
	}

	function addGeoserverLayerIfConfigured(nodeConfiguration, me) {
		var mapping = me.configuration.geoServerLayersMapping;
		if (mapping) {
			var layerPerClass = mapping[nodeConfiguration.className];
			if (layerPerClass) {
				// TODO: More than one GeoServer layer per card
				var layerPerCard = layerPerClass[nodeConfiguration.cardId];
				if (layerPerCard) {
					nodeConfiguration.children = [{
						text: layerPerCard.description,
						cardId: layerPerCard.name,
						className: GEOSERVER,
						leaf: true,
						// the geoserver layer must be visible only
						// if is visible the binded card node
						checked: nodeConfiguration.checked
					}].concat(nodeConfiguration.children);
				}
			}
		}

		return nodeConfiguration;
	}
})();