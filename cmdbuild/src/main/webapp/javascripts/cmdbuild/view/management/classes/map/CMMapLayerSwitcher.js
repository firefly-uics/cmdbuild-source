(function() {

	var EXTERNAL_LAYERS_FOLDER_NAME = "cm_external_layers_folder";
	var CMDBUILD_LAYERS_FOLDER_NAME = "cm_cmdbuild_layers_folder";

	Ext.define("CMDBuild.view.management.map.CMMapLayerSwitcherDelegate", {
		/**
		 * 
		 * @param {Ext.data.NodeInterface} node The Node of the tree that represents the layer
		 * @param {Boolean} checked The new check value
		 */
		onLayerCheckChange: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.map.CMMapLayerSwitcher", {
		extend: "Ext.tree.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.map.CMMapLayerSwitcherDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.rootVisible = false;
			this.useArrows = true;
			this.frame = false;
			this.border = false;
			this.bodyBorder = false;

			this.store = new Ext.create('Ext.data.TreeStore', {
				root : {
					expanded : true,
					children : [{
						text: "@@ Livelli CMDBuild",
						leaf: false,
						expanded: true,
						folderName: CMDBUILD_LAYERS_FOLDER_NAME
					}, {
						text: "@@ Servizi esterni",
						leaf: false,
						expanded: true,
						folderName: EXTERNAL_LAYERS_FOLDER_NAME
					}]
				}
			});

			this.listeners = {
				checkchange: function(node, checked) {
					this.callDelegates("onLayerCheckChange", [node, checked]);
				}
			},

			this.callParent(arguments);
		},

		/**
		 * 
		 * @param {OpenLayers.Layer} layer Add a node to the tree that represent the given layer
		 */
		addLayerItem: function(layer) {
			if (layer.displayInLayerSwitcher) {
				var root = this.getRootNode();
				var targetFolder = layer.isBaseLayer ?
						folderNodeByName(root, EXTERNAL_LAYERS_FOLDER_NAME) :
							folderNodeByName(root, CMDBUILD_LAYERS_FOLDER_NAME);

				try {

					var child = targetFolder.appendChild({
						text: layer.name,
						leaf: true,
						checked: layer.getVisibility(),
						iconCls: "cmdbuild-nodisplay"
					});

					// passing the layerId with the configuration
					// has no effect. There is no time to
					// investigate... do it in the ugly way ;)
					child.layerId = layer.id;
				} catch (e) {
					_debug("Fail to add layer", layer);
				}
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

		/**
		 * 
		 * @param {String} layerId The id to use to identify the node of the tree
		 * @param {Boolean} checked The value to set to the checked property
		 */
		setItemCheckByLayerId: function(layerId, checked) {
			var node = this.getNodeByLayerId(layerId);

			if (node) {
				node.set("checked", checked);
			}
		},

		/**
		 * 
		 * @param {String} layerId The id of the layer to use to retrieve the node
		 * @returns A Ext.data.NodeInterface or null
		 */
		getNodeByLayerId: function(layerId) {
			return this.getRootNode().findChildBy(function(child) {
				return child.layerId == layerId;
			}, null, true);
		}
	});

	function folderNodeByName(root, folderName) {
		return root.findChildBy(function(child) {
			if (child.raw) {
				return child.raw.folderName == folderName;
			} else {
				return false;
			}
		}, null, true);
	}
})();