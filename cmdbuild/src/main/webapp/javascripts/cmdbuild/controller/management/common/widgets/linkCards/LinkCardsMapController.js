(function() {

	Ext.define("CMDBuild.controller.management.common.widgets.linkCards.LinkCardsMapController", {
		extend: "CMDBuild.controller.management.classes.map.CMMapController",

		lastSelection: undefined,

		/*
		 * conf is an object like
		 * {
		 *		view: this.view.mapPanel,
		 *		ownerController: this,
		 *		model: this.model,
		 *		widgetConf: this.widgetConf
		 *	}
		 *
		 */
		constructor: function(conf) {
			var me = this;
			this.view = conf.view;
			this.mapPanel = conf.view; // Use mapPanel for compatibility mode with extended class
			Ext.apply(this, conf);

			if (!_CMCache.isEntryTypeByName(this.widgetConf.className))
				throw 'CMLinkCardsMapController constructor: className not valid';

			this.entryType = _CMCache.getEntryTypeByName(this.widgetConf.className);
			this.classId = this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID);

			this.mon(
				this.view,
				"afterrender",
				function() {
					this.mapPanel.addDelegate(this);

					// set me as delegate of the OpenLayers.Map (pimped in CMMap)
					this.map = this.mapPanel.getMap();
					this.map.delegate = this;

					this.cmIsInEditing = false;

					// init the map state
					this.mapState = new CMDBuild.state.CMMapState(this);
					_CMMapState = this.mapState;

					// set the switcher controller as a map delegate
					var layerSwitcher = this.mapPanel.getLayerSwitcherPanel();
					this.mapPanel.addDelegate(
							new CMDBuild.controller.management.classes
								.CMMapLayerSwitcherController(layerSwitcher, this.map));

					// set me as a delegate of the switcher
					layerSwitcher.addDelegate(this);

					// set me as a delegate of the cardBrowser
					var cardbrowserPanel = this.mapPanel.getCardBrowserPanel();
					if (cardbrowserPanel) {
						new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(cardbrowserPanel, this.mapState);
						cardbrowserPanel.addDelegate(new CMDBuild.controller.management.classes.map.CMCardBrowserDelegate(this));
					}

					// set me as delegate of the mini card grid
					this.mapPanel.getMiniCardGrid().addDelegate(this);

					// init the miniCardGridWindowController
					this.miniCardGridWindowController = new CMDBuild.controller.management.CMMiniCardGridWindowFeaturesController();

					// initialize editing control
					this.editingWindowDelegate = new CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate(this);
					this.mapPanel.editingWindow.addDelegate(this.editingWindowDelegate);
					this.selectControl = new CMDBuild.Management.CMSelectFeatureController([], {
						hover: false,
						renderIntent: "default",
						eventListeners: {
							featurehighlighted: function(e) {
								me.onFeatureSelect(e.feature);
							}
						}
					});

					this.map.addControl(this.selectControl);
					this.selectControl.activate();

					// add me to the CMCardModuleStateDelegates
					_CMCardModuleState.addDelegate(this);
					_CMCardModuleState.setEntryType(this.entryType);

					this.map.events.register("zoomend", this, onZoomEnd);
				},
				this,
				{ single: true }
			);
		},

		getLastSelection: function() {
			return this.lastSelection;
		},

		centerMapOnSelection: function() {
			var me = this;

			if (this.model.hasSelection()) {
				var ss = this.model.getSelections();
				var s = ss[0];

				CMDBuild.ServiceProxy.getFeature(
					this.classId,
					s,
					function onSuccess(result, options, decodedResult) {
						// The card could have no decodedResult
						if (decodedResult.properties) {
							me.map.centerOnGeometry(decodedResult);
						}
					}
				);
			}
		},

		onCardSelected: function(card) {
			if (this.mapPanel.cmVisible) {
				var id = card;
				if (card && typeof card.get == "function")
					id = card.get("Id");

				if (id != this.currentCardId) {
					this.currentCardId = id;
					var layers = this.mapPanel.getMap().getCmdbLayers();

					for (var i=0, l=layers.length; i<l; ++i) {
						layers[i].clearSelection();
						layers[i].selectFeatureByMasterCard(this.currentCardId);
					}
				}

				// To sync the cardBrowserPanelSelection
				if (this.mapPanel.getCardBrowserPanel()) {
					this.mapPanel.getCardBrowserPanel().checkCardNodeAncestors(card);
					this.mapPanel.getCardBrowserPanel().selectCardNodePath(card);
				}

				// To sync the miniCardGrid
				// TODO ensure that the grid is on the right page
				this.mapPanel.getMiniCardGrid().selectCardSilently(card);

				if (card)
					this.centerMapOnFeature(card.data);

				// To sync selected feature with grid card
				this.lastSelection = id;
			}
		}
	});

	/**
	 * Executed after zoomEvent to update mapState object and manually redraw all map's layers
	 */
	function onZoomEnd() {
		var map = this.map;
		var zoom = map.getZoom();
		this.mapState.updateForZoom(zoom);
		var baseLayers = map.cmBaseLayers;
		var haveABaseLayer = false;

		// Manually force redraw of all layers to fix a problem with GoogleMaps
		Ext.Array.each(map.layers, function(item, index, allItems) {
			item.redraw();
		});

		for (var i = 0; i < baseLayers.length; ++i) {
			var layer = baseLayers[i];

			if (!layer || typeof layer.isInZoomRange != 'function')
				continue;

			if (layer.isInZoomRange(zoom)) {
				map.setBaseLayer(layer);
				haveABaseLayer = true;

				break;
			}
		}

		if (!haveABaseLayer)
			map.setBaseLayer(map.cmFakeBaseLayer);
	}

})();