(function() {

	Ext.require([ 'CMDBuild.proxy.gis.Gis', 'CMDBuild.controller.management.classes.map.NavigationTreeDelegate' ]);

	Ext.define("CMDBuild.controller.management.classes.map.CMMapController", {
		// Legacy class name
		alternateClassName : "CMDBuild.controller.management.classes.CMMapController",
		extend : "CMDBuild.controller.management.classes.CMCardDataProvider",

		mixins : {
			observable : "Ext.util.Observable",
			mapDelegate : "CMDBuild.view.management.map.CMMapPanelDelegate",
			editingWindowDelegate : "CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate",
			cardStateDelegate : "CMDBuild.state.CMCardModuleStateDelegate"
		},

		cmfgCatchedFunctions : [],

		cardDataName : "geoAttributes",

		constructor : function(mapPanel, interactionDocument) {
			var me = this;
			this.interactionDocument = interactionDocument;
			if (mapPanel) {
				this.mapPanel = mapPanel;
				this.mapPanel.addDelegate(this);
				this.cmIsInEditing = false;

				var navigationPanel = this.mapPanel.getCardBrowserPanel();
				if (navigationPanel) {
					this.makeNavigationTree(navigationPanel);
				}

				// initialize editing control
				this.makeEditingDelegate();
				this.mapPanel.editingWindow.addDelegate(this.editingWindowDelegate);

				_CMCardModuleState.addDelegate(this);

			} else {
				throw new Error("The map controller was instantiated without a map or the related form panel");
			}
		},
		makeEditingDelegate : function() {
			this.editingWindowDelegate = new CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate(
					this, this.interactionDocument);
		},
		makeNavigationTree : function(navigationPanel) {
			new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(navigationPanel, this.mapState);
			navigationPanel.addDelegate(new CMDBuild.controller.management.classes.map.NavigationTreeDelegate(this,
					this.interactionDocument));
		},

		/*
		 * card could be either a String (the id of the card) or a
		 * Ext.model.Model
		 */
		onCardSelected : function(card) {
			var oldCard = this.interactionDocument.getCurrentCard();
			var cardId = -1;
			var className = "";
			if (card === null) {
				cardId = -1;
				className = oldCard.className;
			} else {
				cardId = card.cardId;
				className = card.className;
			}
			var type = _CMCache.getEntryTypeByName(className);
			if (cardId !== -1) {
				this.setCard({
					cardId : cardId,
					className : className
				});
			}
			this.interactionDocument.setCurrentCard({
				cardId : cardId,
				className : className
			});
			if (!this.mapPanel.cmVisible) {
				this.interactionDocument.resetZoom();
			}
			else if (cardId !== -1) {
				var card = {
					className : className,
					cardId : cardId
				};
				this.interactionDocument.centerOnCard(card, function(center) {
					if (!center) {
						var mapPanel = this.interactionDocument.getMapPanel();
						mapPanel.center(this.interactionDocument.configurationMap);
					}
					this.interactionDocument.changed();
				}, this);
			} else {
				if (!oldCard || className !== oldCard.className) {
					var mapPanel = this.interactionDocument.getMapPanel();
					mapPanel.center(this.interactionDocument.configurationMap);

				}
				this.interactionDocument.changed();

			}
			this.interactionDocument.setNoZoom(false);
		},

		setCard : function(card, callback, callbackScope) {
		},
		editMode : function() {
			this.cmIsInEditing = true;

			if (this.mapPanel.cmVisible) {
				this.mapPanel.editMode();
				this.deactivateSelectControl();
			}
		},

		displayMode : function() {
			this.cmIsInEditing = false;

			if (this.mapPanel.cmVisible) {
				this.mapPanel.displayMode();
				this.activateSelectControl();
			}
		},

		onCardSaved : function(c) {
			if (this.mapPanel.cmVisible) {
				var me = this;

				var type = _CMCache.getEntryTypeById(c.IdClass);
				var card = {
					cardId : c.Id,
					className : type.get("name")
				};
				this.setCard(card, this.callBackSetCard);
			}
		},
		callBackCenter : function() {
			this.interactionDocument.changed();
			this.interactionDocument.changedFeature();
		},
		callBackSetCard : function(card) {
			this.mapPanel.getMap().changeFeatureOnLayers(card.cardId);
			this.interactionDocument.setCurrentCard(card);
			this.interactionDocument.centerOnCard(card, this.callBackCenter, this);

		},
		deactivateSelectControl : function() {
			// this.selectControl.deactivate();
		},

		activateSelectControl : function() {
			// this.selectControl.activate();
		},

		onEntryTypeSelected : onEntryTypeSelected,
		getCardData : getCardData,

		/* As mapDelegate ******** */

		onMapPanelVisibilityChanged : onVisibilityChanged,

		/* As CMCardModuleStateDelegate ************** */

		onEntryTypeDidChange : function(state, entryType, danglingCard) {
			this.onEntryTypeSelected(entryType, danglingCard);
		},

		onCardDidChange : function(state, card) {
			if (card != null) {
				var type = _CMCache.getEntryTypeById(card.get("IdClass"));
				this.onCardSelected({
					cardId : card.get("Id"),
					className : type.get("name")

				});
			}
		},

		/* As CMMap delegate *************** */

		featureWasAdded : function(feature) {
		},

		// As CMDBuild.state.CMMapStateDelegate

		geoAttributeUsageChanged : function(geoAttribute) {
		},

		geoAttributeZoomValidityChanged : function(geoAttribute) {
			if (!geoAttribute.isZoomValid()) {
				removeLayerForGeoAttribute(this.map, geoAttribute, this);
			} else {
				addLayerForGeoAttribute(this.map, geoAttribute, this);
			}
		},

		featureVisibilityChanged : function(className, cardId, visible) {
		},

		getCurrentCardId : function() {
			return this.currentCardId;
		},

		getCurrentMap : function() {
			return this.map;
		}
	});

	function getLayerVisibility(id, bindings, visibles) {
		for (var i = 0; i < bindings.length; i++) {
			if (Ext.Array.contains(visibles, bindings[i].className)) {
				if (bindings[i].idCard == id) {
					return true;
				}
			}
		}
		return false;
	}

	function buildLongPressController(me) {
		var map = me.map;
		var longPressControl = new OpenLayers.Control.LongPress({
			onLongPress : function(e) {
				var lonlat = map.getLonLatFromPixel(e.xy);
				var features = map.getFeaturesInLonLat(lonlat);

				// no features no window
				if (features.length == 0) {
					return;
				}

			}
		});

		map.addControl(longPressControl);
		longPressControl.activate();
	}

	function getCardData(params) {
		var cardId = params.cardId;
		var className = params.cardIdclassName;
		var geo = this.mapPanel.getMap().getGeometries(cardId, className);
		return Ext.JSON.encode(geo);
	}

	function onEntryTypeSelected(entryType, danglingCard) {
		if (!entryType || !this.mapPanel.cmVisible) {
			return;
		}

		var newEntryTypeId = entryType.get("id");
		var lastCard = _CMCardModuleState.card;
		if (this.currentClassId != newEntryTypeId) {
			this.currentClassId = newEntryTypeId;
			lastCard = undefined;
		}

	}

	function onVisibilityChanged(map, visible) {
		if (visible) {
			var lastClass = _CMCardModuleState.entryType, lastCard = _CMCardModuleState.card;

			if (lastClass && this.currentClassId && this.currentClassId != lastClass.get("id")) {

				this.onEntryTypeSelected(lastClass, {
					Id : lastCard.get("Id")
				});
			}

		} else {
			if (this.cmIsInEditing) {
				this.mapPanel.displayMode();
			}
		}
	}

	function onLayerVisibilityChange(param) {
		var layer = param.object;

		var cardBrowserPanel = this.mapPanel.getCardBrowserPanel();
		if (layer.CM_geoserverLayer && cardBrowserPanel) {
			cardBrowserPanel.udpateCheckForLayer(layer);
		}
	}

})();
