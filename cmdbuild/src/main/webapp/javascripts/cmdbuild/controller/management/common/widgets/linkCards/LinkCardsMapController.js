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
								me.onFeatureSelect(e);
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

		onCardSelected: function(card) {
_debug('onCardSelected', card);
			if (this.mapPanel.cmVisible) {
				var id = card;

				if (card && typeof card.get == "function")
					id = card.get("Id");
_debug('onCardSelected id', id);
_debug('onCardSelected this.currentCardId', this.currentCardId);
				if (id != this.currentCardId) {
_debug('onCardSelected if');
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
				this.mapPanel.getMiniCardGrid().selectCardSilently(card);

				if (card) {
_debug('centerMapOnFeature', card.data);
					this.centerMapOnFeature(card.data);
				}
				// To sync selected feature with grid card
				this.model.select(id);
				this.lastSelection = id;
//				this.ownerController.loadPageForLastSelection(id);
			}
		},

		/**
		 * @param {Object} e
		 * @param {OpenLayers.Feature.Vector} e.feature
		 * @param {CMDBuild.Management.CMSelectFeatureController} e.object
		 */
		onFeatureSelect: function(e) {
_debug('featurehighlighted', e);
_debug('featurehighlighted getMouse', e.object.handlers.feature.evt.xy);
_debug('featurehighlighted coords', this.map.getLonLatFromPixel(e.object.handlers.feature.evt.xy));
			var feature = e.feature;
			var prop = feature.attributes;
			var layer = feature.layer;

			if (layer.editLayer) { // the feature selected is in a cmdbLayer with an associated editLayer
				_CMCardModuleState.setCard({
					Id: prop.master_card,
					IdClass: prop.master_class
				});
			}
		},
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