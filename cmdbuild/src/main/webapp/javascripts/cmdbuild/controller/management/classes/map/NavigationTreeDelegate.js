(function() {

	/**
	 * This is the implementation of the CMCardBrowserTreeDelegate for the
	 * CMMapController.
	 */
	Ext.define("CMDBuild.controller.management.classes.map.NavigationTreeDelegate", {
		extend : "CMDBuild.view.management.classes.map.NavigationTreeDelegate",
		master : undefined,
		interactionDocument : undefined,
		constructor : function(master, interactionDocument) {
			this.interactionDocument = interactionDocument;
			this.master = master;
		},

		// Hide or show the feature[s] for the node
		// from the map.
		// the action has effect over all the branch that start with the
		// passed node.
		// So, if the node was never opened,
		// there aren't the info to show/hide the features.
		// For this reason, act like an expand, loading the
		// branch at all, and then show/hide the features.
		onCardBrowserTreeCheckChange : function(tree, node, checked, deeply) {
			if (!(checked || node.raw.checked))
				return;
			node.raw.checked = true;
			if (deeply) {
				setFeatureVisibilityForAllBranch(tree, this.master, node, checked);
			} else {
				setCardFeaturesVisibility(tree, this.master, node, checked);
			}
		},

		onCardBrowserTreeCardSelected : function(cardBaseInfo) {
			if (cardBaseInfo.IdClass > 0) {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', cardBaseInfo);
			}
		},

		onCardBrowserTreeNodeAppend : function(cardBrowserTree, node) {
			if (cardBrowserTree && node.getCMDBuildClassName() == cardBrowserTree.dataSource.GEOSERVER
					&& node.parentNode != null) {
				// add a reference to the geoServer node to be
				// able to check this node when check its parent
				// see setCardFeaturesVisibility
				node.parentNode.cmGeoServerNode = node;
			}
		},

		onCardBrowserTreeActivate : function(cardBrowserTree, activationCount) {
		},
		onCardBrowserTreeItemExpand : function(tree, node) {
		},

		/**
		 * @param {Object}
		 *            card
		 * @param {Number}
		 *            card.Id
		 * @param {Number}
		 *            card.IdClass
		 * 
		 * @returns {Void}
		 */
		onCardNavigation : function(card) {
			if (!card.id) {
				card.id = card.Id;
			}
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', card);
		},
		onCardZoom : function(card) {
			this.interactionDocument.centerOnCard(card, function() {
			}, this);
		}
	});

	function setFeatureVisibilityForAllBranch(tree, master, node, checked) {
		setCardFeaturesVisibility(tree, master, node, checked);

		if (node.get("loading")) {
			return;
		}

		var children = node.childNodes || node.children || [];
		setChildrenFeaturesVisibility(tree, master, checked, children, true);
	}

	function setCardFeaturesVisibility(tree, master, node, visibility) {
	}

	function setChildrenFeaturesVisibility(tree, master, checked, children) {
		for (var i = 0, child = null; i < children.length; ++i) {
			child = children[i];
			child.setChecked(checked);
			setFeatureVisibilityForAllBranch(tree, master, child, checked);
		}
	}

	function setGeoserverLayerVisibility(master, node, checked) {
		var layerName = node.data.cardId;
		var l = master.map.getGeoServerLayerByName(layerName);
		if (l) {
			l.setVisibility(checked);
		}
	}
})();