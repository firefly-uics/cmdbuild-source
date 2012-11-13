(function() {
	var GEOSERVER = "GeoServer";

	Ext.define("CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource", {
		GEOSERVER: GEOSERVER,
		constructor: function(cardBrowserTree, mapState) {
			this.cardBrowserTree = cardBrowserTree;
			this.mapState = mapState;
			/*
			 * The configuration has a tree like representation
			 * of a domain chain. Start with a root node
			 * with only the className to load, and a children array
			 * in which configure the domains to use to
			 * retrieve the referenced cards.
			 * 
			 * Follow an example
			 * 
			 *{
			 * 	className: "Building",
			 * 	children: [{
			 * 		className: "Floor",
			 * 		domainName: "BuildingFloor",
			 * 		direct: true,
			 * 		children: [{
			 * 			className: "Room",
			 * 			domainName: "FloorRoom",
			 * 			direct: true,
			 * 			children: [{
			 * 				className: "Asset",
			 * 				domainName: "RoomAsset",
			 * 				direct: true
			 * 			}, {
			 * 				className: "Workplace",
			 * 				domainName: "RoomWorkplace",
			 * 				direct: true
			 * 			}]
			 * 		}]
			 * 	}]
			 *}
			 * This would load a tree with the building
			 * as first level. Then when expand a building
			 * you can find the floors of that building, than
			 * the rooms of a given floor, and at last the assets and
			 * work-places at the same level
			 */
			this.configuration = CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration;

			var me = this;
			var targetNode = me.cardBrowserTree.getRootNode();

			if (!targetNode) {
				return;
			}

			var rootName = me.configuration.root.className;
			var rootDescription = me.configuration.root.description || me.configuration.root.className;

			// change the label of the root node
//			targetNode.set("text", rootDescription);

			// fill the first level of tree nodes
			// asking the cards according to the 
			// root of the configuration
			CMDBuild.ServiceProxy.getCardBasicInfoList(
				rootName, //
				function successGetCardBasicInfoList(operation, options, response) {
					var cards = response.rows;
					for (var i=0, card=null, l=cards.length; i<l; ++i) {
						card = cards[i];
						card.expansibleDomains = me.configuration.root.children || [];
						me.cardBrowserTree.addChildToNode(targetNode, adaptCardToNode(card, me));
					}
				}
			);

			this.callParent(arguments);
			this.cardBrowserTree.setDataSource(this);
		},

		loadBranch: function(node) {
			var deeply = true;
			loadChildren(this, node, deeply);
		},

		loadChildren: function(node, cb) {
			var deeply = false;
			loadChildren(this, node, deeply, cb);
		}
	});

	function loadChildren(me, node, deeply, cb) {

		if (node.didChildrenLoaded() || node.get("loading")) {
			return;
		}

		var domains = node.getExpansibleDomains();
		if (domains.length == 0) {
			return;
		}

		var totalChildren = node.childNodes || [];
		var steps = 0;
		node.set("loading", true);
		for (var i=0, domain=null; i<domains.length; ++i) {
			domain = domains[i];
			loadDomainChildrenForNode(me, node, domain, deeply, //
				function(domainChildren) {
					totalChildren = totalChildren.concat(domainChildren);
					node.set("loading", false);
					if (++steps == domains.length
							&&typeof cb == "function") {
	
						cb(totalChildren);
					}
				}
			);
		}
	}

	function loadDomainChildrenForNode(me, node, domain, deeply, cb) {
		var cachedDomain = _CMCache.getDomainByName(domain.domainName);

		if (!cachedDomain) {
			return;
		}

		CMDBuild.ServiceProxy.relations.getList({
			params: {
				Id: node.getCardId(),
				ClassName: node.getCMDBuildClassName(),
				domainId: cachedDomain.getId(),
				src: domain.direct ? "_1" : "2"
			},
			success: function(operation, options, response) {
				var relations = [];
				var children = [];

				if (response.domains.length > 0) {
					relations = response.domains[0].relations || [];
				}

				for (var j=0, rel = null, l=relations.length; j<l; ++j) {
					rel = relations[j];
					var newChild = me.cardBrowserTree.addLoadedChildren(node,
							adaptRelationListResponseToNode(rel, domain, me));

					children.push(newChild);

					if (deeply) {
						loadChildren(me, newChild, deeply);
					}
				}

				if (typeof cb == "function") {
					cb(children);
				}
			}
		});
	}

	function adaptCardToNode(card, me) {
		var nodeConf = {
			text: card.Description,
			className: card.IdClass_value,
			classId: card.IdClass,
			cardId: card.Id,
			expansibleDomains: card.expansibleDomains,
			leaf: card.expansibleDomains.length == 0,
			expanded: false,
			checked: true
		};

		return addGeoserverLayerIfConfigured(nodeConf, me);
	}

	function adaptRelationListResponseToNode(rel, domain, me) {
		var subDomains = domain.children || [];
		var nodeConf = {
			text: rel.dst_desc || rel.dst_code,
			className: _CMCache.getEntryTypeNameById(rel.dst_cid),
			classId: rel.dst_cid,
			cardId: rel.dst_id,
			expansibleDomains: subDomains,
			leaf: subDomains.length == 0,
			expanded: false,
			checked: true
		};

		return addGeoserverLayerIfConfigured(nodeConf, me);
	}

	function addGeoserverLayerIfConfigured(nodeConfiguration, me) {
		var mapping = me.configuration.geoServerLayersMapping;
		if (mapping) {
			var layerPerClass = mapping[nodeConfiguration.className];
			if (layerPerClass) {
				var layerPerCard = layerPerClass[nodeConfiguration.cardId];
				if (layerPerCard) {
					nodeConfiguration.children = [{
						text: layerPerCard.description,
						cardId: layerPerCard.name,
						className: GEOSERVER,
						leaf: true,
						checked: me.mapState.isGeoAttributeVisibleToUser({
							name: layerPerCard.name
						})
					}];
				}
			}
		}

		return nodeConfiguration;
	}
})();