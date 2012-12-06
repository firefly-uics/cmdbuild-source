(function() {

	/**
	 * This is the implementation of the CMCardBrowserTreeDelegate for the
	 * CMMapController.
	 */
	Ext.define("CMDBuild.controller.management.classes.map.CMCardBrowserDelegate", {
		extend: "CMDBuild.view.management.CMCardBrowserTreeDelegate",

		constructor: function(master) {
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

		onCardBrowserTreeCheckChange: function(tree, node, checked, deeply) {
			if (deeply) {
				setFeatureVisibilityForAllBranch(tree, this.master, node, checked);
			} else {
				setCardFeaturesVisibility(tree, this.master, node, checked);
			}
		},

		onCardBrowserTreeItemExpand: function(tree, node) {
			tree.dataSource.loadChildren(node);
		},

		onCardBrowserTreeCardSelected: function(cardBaseInfo) {
			_CMMainViewportController.openCard(cardBaseInfo);
		},

		onCardBrowserTreeItemAdded: function(tree, targetNode, newNode) {
			var card = _CMCardModuleState.card;
			var cardBrowserPanel = this.master.mapPanel.getCardBrowserPanel();
			if (cardBrowserPanel
					&& newNode.isBindingCard(card)) {
				cardBrowserPanel.selectNodeSilently(newNode);
			}
		},

		onCardBrowserTreeActivate: function(cardBrowserTree, activationCount) {
			// init the cardBrowserDataSource
			if (activationCount == 1) {
				new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(cardBrowserTree, this.master.mapState);
			}
		}
	});

	function setFeatureVisibilityForAllBranch(tree, master, node, checked) {
		setCardFeaturesVisibility(tree, master, node, checked);

		if (node.get("loading")) {
			return;
		}

		if (node.didChildrenLoaded()) {
			var children = node.childNodes || [];
			setChildrenFeaturesVisibility(tree, master, checked, children, true);
		} else {
			tree.dataSource.loadChildren(node, function(children) {
				setChildrenFeaturesVisibility(tree, master, checked, children, true);
			});
		}
	}

	function setCardFeaturesVisibility(tree, master, node, visibility) {
		var className = node.getCMDBuildClassName();
		var cardId = node.getCardId();
		if (node.data.className != tree.dataSource.GEOSERVER) {
			master.mapState.setFeatureVisisbility(className, cardId, visibility);
		} else {
			setGeoserverLayerVisibility(master, node, visibility);
		}
	}

	function setChildrenFeaturesVisibility(tree, master, checked, children) {
		for (var i=0, child=null; i<children.length; ++i) {
			child = children[i];
			child.set("checked", checked);
			setFeatureVisibilityForAllBranch(tree, master, child, checked);
		}
	}

	function setGeoserverLayerVisibility(master, node, checked) {
		var l = master.map.getGeoServerLayerByName(node.data.cardId);
		if (l) {
			l.setVisibility(checked);
		}
	}
})();