(function () {
	var EXTERNAL_LAYERS_FOLDER_NAME = "cm_external_layers_folder";
	var CMDBUILD_LAYERS_FOLDER_NAME = "cm_cmdbuild_layers_folder";
	var GEOSERVER_LAYERS_FOLDER_NAME = "cm_geoserver_layers_folder";
	Ext.define('CMDBuild.view.management.classes.map.CM16LayerTree', {
		extend: "Ext.tree.Panel",

		requires: [
			'CMDBuild.proxy.gis.Layer'
		],

		/**
		 * @cfg {CMDBuild.controller.management.classes.map.CM16LayerTree}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,
		
		map: undefined,

		constructor: function () {
			var store = CMDBuild.proxy.gis.Layer.getStore();
			this.store = store;
			this.callParent(arguments);
		},
		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent: function () {
			this.interactionDocument.observe(this);
			this.callParent(arguments);
		},

		listeners: {
			checkchange: function ( node, checked, eOpts ) {
				this.interactionDocument.getLayerByName(node.raw.layerName, function(layer) {
					this.delegate.cmfg( 'onVisibilityChange', {
						checked : checked,
						layer : layer
					});
				}, this);
			}
		},
		
		/**
		 * @param {Object} record
		 * @param {String} record.Id
		 * @param {String} record.IdClass
		 * 
		 * @returns {Void}
		 */
		navigateOnCard : function(record) {			
			this.delegate.cmfg( 'onCardNavigation', {
				Id : record .get('Id'),
				IdClass : record .get('IdClass')
			});
		},
		refresh : function() {
			var currentClassId = (Ext.isEmpty(_CMCardModuleState.entryType)) ?
					undefined : _CMCardModuleState.entryType.getId();
			if (! currentClassId) {
				return;
			}
			var currentCardId = (Ext.isEmpty(_CMCardModuleState.card)) ?
					undefined : _CMCardModuleState.card.raw.Id;//getId();
			var currentClassName = (Ext.isEmpty(_CMCardModuleState.entryType)) ?
					undefined : _CMCardModuleState.entryType.getName();
			var me = this;
			this.interactionDocument.getAllLayers(function(layers) {
				var root = me.getRootNode();
				for (var i = 0; i < layers.length; i++) {
					var node = nodeByLayerName(root, layers[i].name);
					if (! me.interactionDocument.isVisible(layers[i], currentClassName, currentCardId)) {
						if (node) {
							node.parentNode.removeChild(node);
						}
					}
					else if (node) {
						var hide = me.interactionDocument.isHide(layers[i]);
						node.set('checked', ! hide);
					}
					else {
						me.addLayerItem(layers[i]);
					}
				}
				expandBaseFolders(me.getRootNode(), me);
			});
		},	
		/**
		 * 
		 * @param {OpenLayers.Layer} layer Add a node to the tree that represent the given layer
		 */
		addLayerItem: function(layer) {
			var targetFolder = retrieveTargetFolder(layer, this.getRootNode());
			var currentClassName = (Ext.isEmpty(_CMCardModuleState.entryType)) ?
					undefined : _CMCardModuleState.entryType.getName();
			var currentCardId = (Ext.isEmpty(_CMCardModuleState.card)) ?
					undefined : _CMCardModuleState.card.raw.Id;

			try {

				var child = targetFolder.appendChild({
					text: layer.name,
					layerName : layer.name,
					leaf: true,
					checked: this.interactionDocument.isVisible(layer, currentClassName, currentCardId),
					iconCls: "cmdbuild-nodisplay"
				});

				// passing the layerId with the configuration
				// has no effect. There is no time to
				// investigate... do it in the ugly way ;)
				child.layerId = layer.id;
				child.layerIndex = layer.cmdb_index;
			} catch (e) {
				console.log("Fail to add layer", layer);
			}
		},

		/**
		 * 
		 * @param {OpenLayers.Layer} layer Removes from the tree the layer that representes the given layer
		 */
		removeLayerItem: function(layer) {
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
		} else {
			targetFolder = nodeByLayerName(root, CMDBUILD_LAYERS_FOLDER_NAME);
		}

		return targetFolder;
	}

	function expandBaseFolders(root, me) {
		var externalServicesFolder = nodeByLayerName(root, EXTERNAL_LAYERS_FOLDER_NAME);
		var cmdbuildFolder = nodeByLayerName(root, CMDBUILD_LAYERS_FOLDER_NAME);
		var geoServerFolder = nodeByLayerName(root, GEOSERVER_LAYERS_FOLDER_NAME);
		if (externalServicesFolder && externalServicesFolder.childNodes.length > 0) {
			me.expandNode(externalServicesFolder);
		}
		if (cmdbuildFolder && cmdbuildFolder.childNodes.length > 0) {
			me.expandNode(cmdbuildFolder);
		}
		if (geoServerFolder && geoServerFolder.childNodes.length > 0) {
			me.expandNode(geoServerFolder);
		}
		
	}

	function retrieveGeoserverFolder(root) {
		var externalServicesFolder = nodeByLayerName(root, EXTERNAL_LAYERS_FOLDER_NAME);
		var geoserverFolder = nodeByLayerName(externalServicesFolder, GEOSERVER_LAYERS_FOLDER_NAME);

		if (!geoserverFolder) {
			geoserverFolder = externalServicesFolder.appendChild({
				text: CMDBuild.Translation.administration.modcartography.geoserver.title,
				leaf: false,
				expanded: false,
				layerName: GEOSERVER_LAYERS_FOLDER_NAME,
				checked: true
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
						
