(function() {

	Ext.require([ 'CMDBuild.proxy.gis.Gis' ]);

	Ext.define("CMDBuild.controller.management.classes.map.CMMapController", {
		alternateClassName : "CMDBuild.controller.management.classes.CMMapController", // Legacy
		// class
		// name
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

				var cardbrowserPanel = this.mapPanel.getCardBrowserPanel();
				if (cardbrowserPanel) {
					new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(cardbrowserPanel,
							this.mapState);
					cardbrowserPanel.addDelegate(new CMDBuild.controller.management.classes.map.CMCardBrowserDelegate(
							this));
				}

				// initialize editing control
				this.editingWindowDelegate = new CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate(
						this, this.interactionDocument);
				this.mapPanel.editingWindow.addDelegate(this.editingWindowDelegate);

				_CMCardModuleState.addDelegate(this);

			} else {
				throw new Error("The map controller was instantiated without a map or the related form panel");
			}
		},

		/*
		 * card could be either a String (the id of the card) or a
		 * Ext.model.Model
		 */
		onCardSelected : function(card) {
			if (card === null) {
				return;
			}
			var cardId = card.cardId;
			var className = card.className;
			var type = _CMCache.getEntryTypeByName(className);
			if (cardId !== -1) {
				_CMCardModuleState.setCard({
					cardId : cardId,
					className : className,
				});
			}
			this.interactionDocument.setCurrentCard({
				cardId : cardId,
				className : className
			});
			if (cardId !== -1) {
				this.interactionDocument.centerOnCard({
					className : className,
					cardId : cardId
				});
			}
			this.interactionDocument.changed();
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
			/*
			 * Normally after the save, the main controller say to the grid to
			 * reload it, and select the new card. If the map is visible on
			 * save, this could not be done, so say to this controller to
			 * refresh the features loaded, and set the new card as selected
			 */
			if (this.mapPanel.cmVisible) {
				var me = this;

				_CMCardModuleState.setCard({
					Id : c.Id,
					IdClass : c.IdClass
				}, function(card) {
					me.mapPanel.getMap().changeIdOnLayers(-1, c.Id);
					var type = _CMCache.getEntryTypeById(c.IdClass);
					me.interactionDocument.setCurrentCard({
						cardId : c.Id,
						className : type.get("name")
					});
					me.interactionDocument.changed();
					me.interactionDocument.changedFeature();
				});
			}
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

	function loadCardGridStore(gridController) {
		gridController.onCardGridShow();
	}

	function updateCardGridTitle(entryType, gridController) {
		var grid = gridController.getView();
		var prefix = CMDBuild.Translation.management.modcard.title;
		grid.setTitle(prefix + entryType.get("name"));
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

		if (danglingCard) {
			this.onCardSelected({
				cardId : danglingCard.Id,
				className : entryType.get("name")
			});
		} else {
			this.onCardSelected({
				cardId : -1,
				className : entryType.get("name")
			});
		}
	}

	function onVisibilityChanged(map, visible) {
		if (visible) {
			var lastClass = _CMCardModuleState.entryType, lastCard = _CMCardModuleState.card;

			if (lastClass && this.currentClassId != lastClass.get("id")) {

				this.onEntryTypeSelected(lastClass);
			} else {
				if (lastCard && (!this.currentCardId || this.currentCardId != lastCard.get("Id"))) {

					this.onCardSelected({
						cardId : lastCard.get("Id"),
						className : (lastCard.get("className")) ? lastCard.get("className") : lastCard.raw.className
					});
				}
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
	;

	function sortAttributesByIndex(geoAttributes) {
		var cmdbuildLayers = [];
		var geoserverLayers = [];
		for (var i = 0, l = geoAttributes.length; i < l; ++i) {
			var attr = geoAttributes[i];
			if (attr.masterTableId) {
				cmdbuildLayers[attr.index] = attr;
			} else {
				geoserverLayers[attr.index] = attr;
			}
		}

		return cmdbuildLayers.concat(geoserverLayers);
	}

})();
