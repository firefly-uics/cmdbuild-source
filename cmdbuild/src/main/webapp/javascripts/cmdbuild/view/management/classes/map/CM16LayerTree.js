(function() {
	var EXTERNAL_LAYERS_FOLDER_NAME = "cm_external_layers_folder";
	var CMDBUILD_LAYERS_FOLDER_NAME = "cm_cmdbuild_layers_folder";
	var GEOSERVER_LAYERS_FOLDER_NAME = "cm_geoserver_layers_folder";
	var THEMATISM_LAYERS_FOLDER_NAME = "cm_thematism_layers_folder";
	Ext.define('CMDBuild.view.management.classes.map.CM16LayerTree', {
		extend : "Ext.tree.Panel",

		requires : [ 'CMDBuild.proxy.gis.Layer' ],

		/**
		 * @cfg {CMDBuild.controller.management.classes.map.CM16LayerTree}
		 */
		delegate : undefined,

		border : false,
		cls : 'cmdb-border-bottom',
		frame : false,

		map : undefined,

		oldClassName : undefined,

		constructor : function() {
			var store = CMDBuild.proxy.gis.Layer.getStore();
			CMDBuild.proxy.gis.Layer.readAll({
				scope : this,
				callback : function(a, b, response) {
					var me = this;
				}
			});

			this.store = store;
			this.callParent(arguments);
		},
		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			this.interactionDocument.observe(this);
			this.callParent(arguments);
		},
		checkNode : function(node, checked) {
			if (node.raw.leaf) {
				this.interactionDocument.getLayerByName(node.raw.layerName, function(layer) {
					if (!layer) {
						layer = this.interactionDocument.getThematicLayerByName(node.raw.layerName);
					}
					this.delegate.cmfg('onVisibilityChange', {
						checked : checked,
						layer : layer
					});

				}, this);
			} else {
				var nodes = node.childNodes;
				for (var i = 0; i < nodes.length; i++) {
					this.checkNode(nodes[i], checked);
				}
			}
		},
		listeners : {
			checkchange : function(node, checked, eOpts) {
				this.checkNode(node, checked);
			}
		},

		/**
		 * @param {Object}
		 *            record
		 * @param {String}
		 *            record.Id
		 * @param {String}
		 *            record.IdClass
		 * 
		 * @returns {Void}
		 */
		navigateOnCard : function(record) {
			this.delegate.cmfg('onCardNavigation', {
				Id : record.get('Id'),
				IdClass : record.get('IdClass')
			});
		},
		refresh : function() {
			var currentCard = this.interactionDocument.getCurrentCard();
			var cl = _CMCache.getEntryTypeByName(currentCard.className);
			var currentClassId = cl.get("id");
			if (!currentClassId) {
				return;
			}
			var currentCardId = currentCard.cardId;
			var currentClassName = currentCard.className;
			var me = this;
			this.interactionDocument.getAllLayers(function(layers) {
				var root = me.getRootNode();
				for (var i = 0; i < layers.length; i++) {
					var node = nodeByLayerName(root, layers[i].name);
					if (!me.interactionDocument.isVisible(layers[i], currentClassName, currentCardId)) {
						; // nop
					} else if (node) {
						var hide = me.interactionDocument.isHide(layers[i]);
						node.set('checked', !hide);
					} else {
						me.addLayerItem(layers[i]);
					}
				}
				var thematicLayers = me.interactionDocument.getThematicLayers();
				for (var i = 0; i < thematicLayers.length; i++) {
					var node = nodeByLayerName(root, thematicLayers[i].name);
					if (!node) {
						me.addLayerItem(thematicLayers[i]);
					}
				}
				if (this.oldClassName !== currentClassName) {
					me.expandAll();
					this.oldClassName = currentClassName;
				}
			});
		},

		/**
		 * 
		 * @param {ol.Layer}
		 *            layer Add a node to the tree that represent the given
		 *            layer
		 *            
		 * @returns {Void}
		 */
		addLayerItem : function(layer) {
			var targetFolder = retrieveTargetFolder(layer, this.getRootNode());
			var currentClassName = (Ext.isEmpty(_CMCardModuleState.entryType)) ? undefined
					: _CMCardModuleState.entryType.getName();
			var currentCardId = (Ext.isEmpty(_CMCardModuleState.card)) ? undefined : _CMCardModuleState.card.raw.Id;

			try {

				var child = targetFolder.appendChild({
					text : layer.name,
					layerName : layer.name,
					leaf : true,
					checked : true,// this.interactionDocument.isVisible(layer,
					// currentClassName, currentCardId),
					iconCls : "cmdbuild-nodisplay"
				});

				// passing the layerId with the configuration
				// has no effect. There is no time to
				// investigate... do it in the ugly way ;)
				child.layerId = layer.id;
				child.layerIndex = layer.cmdb_index;
			} catch (e) {
				console.log("Fail to add layer", layer);
			}
			this.expandNode(targetFolder);
		},

		/**
		 * 
		 * @param {OpenLayers.Layer}
		 *            layer Removes from the tree the layer that represents the
		 *            given layer
		 */
		removeLayerItem : function(layer) {
			var node = this.getNodeByLayerId(layer.id);

			if (node) {
				node.remove(true);
			}
		},
	});

	function retrieveTargetFolder(layer, root) {
		var targetFolder = null;

		if (layer.isBaseLayer) {
			targetFolder = nodeByLayerName(root, EXTERNAL_LAYERS_FOLDER_NAME);
		} else if (layer.masterTableName === "_Geoserver") {
			targetFolder = retrieveGeoserverFolder(root);
		} else if (layer.masterTableName === "_Thematism") {
			targetFolder = retrieveThematismFolder(root);
		} else {
			targetFolder = nodeByLayerName(root, CMDBUILD_LAYERS_FOLDER_NAME);
		}

		return targetFolder;
	}

	function retrieveThematismFolder(root) {
		var thematismFolder = nodeByLayerName(root, THEMATISM_LAYERS_FOLDER_NAME);

		if (!thematismFolder) {
			thematismFolder = root.appendChild({
				text : "@@ Thematism",
				leaf : false,
				expanded : false,
				layerName : THEMATISM_LAYERS_FOLDER_NAME,
				checked : true
			});
		}

		return thematismFolder;
	}
	function retrieveGeoserverFolder(root) {
		var externalServicesFolder = nodeByLayerName(root, EXTERNAL_LAYERS_FOLDER_NAME);
		var geoserverFolder = nodeByLayerName(externalServicesFolder, GEOSERVER_LAYERS_FOLDER_NAME);

		if (!geoserverFolder) {
			geoserverFolder = externalServicesFolder.appendChild({
				text : CMDBuild.Translation.administration.modcartography.geoserver.title,
				leaf : false,
				expanded : false,
				layerName : GEOSERVER_LAYERS_FOLDER_NAME,
				checked : true
			});
		}

		return geoserverFolder;
	}

	function nodeByLayerName(root, layerName) {
		return root.findChildBy(function(child) {
			return child.raw.layerName === layerName;
		}, null, true);
	}
})();
