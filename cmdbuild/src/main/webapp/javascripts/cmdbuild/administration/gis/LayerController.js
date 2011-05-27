(function() {
	
	var searchNodeById = function(tree, id) {
		return CMDBuild.TreeUtility.searchNodeByAttribute({
			attribute: "id",
			value: id,
			root: tree
		});
	};
	
	var treeVisit = function(startingNodeId, fn) {
		var classTree = CMDBuild.Cache.getClassesTree();
		var tableNode = searchNodeById(classTree, startingNodeId);
		
		var nodeToVisit = [tableNode];
		while (nodeToVisit.length > 0) {
			var node = nodeToVisit.pop();
			var cachedTable = CMDBuild.Cache.getTableById(node.attributes.id);
			if (cachedTable) {
				fn.call(cachedTable);				
			}
			nodeToVisit = nodeToVisit.concat(node.childNodes);
		}
		this.publish("cmdb-update-geoattr", startingNodeId);
	};
	
	var getGeoAttrByName = function(list, name) {
		var out = null;
		for (var i=0, l=list.length; i<l; ++i) {
			var attr = list[i];
			if (attr.name == name) {
				out = attr;
				break;
			}
		}
		return out;
	};
	
	var onNewGeoattr = function(decoded) {
		var tableId =  decoded.geoAttribute.masterTableId;
		var geoAttr = decoded.geoAttribute;
		// scope --> cachedTable
		var addGeoAttr = function() {
			// the new geo attribute is always visible
			var dollyGeoAttr = Ext.apply({}, { isvisible : true }, geoAttr);
			this.meta.geoAttributes.push(dollyGeoAttr);
		};
		treeVisit.call(this, tableId, addGeoAttr);
	};
	
	var onModifyGeoattr = function(decoded) {
		var tableId =  decoded.geoAttribute.masterTableId;
		var geoAttr = decoded.geoAttribute;
		var attrName = decoded.geoAttribute.name;
		// scope --> cachedTable
		var updateGeoAttr = function() {
			var geoAttrs = this.meta.geoAttributes;
			var attr = getGeoAttrByName(geoAttrs, attrName);
			if (attr) {
				var dollyGeoAttr = Ext.apply({}, {isvisible: attr.isvisible}, geoAttr);
				geoAttrs.remove(attr);
				geoAttrs.push(dollyGeoAttr);
			}
		};
		treeVisit.call(this, tableId, updateGeoAttr);
	};
	
	var onDeleteGeoattr = function(decoded) {	
		var tableId =  decoded.table.id;
		var attrName = decoded.geoAttribute.name;
		// scope --> cachedTable
		var deleteAttr = function() {
			var geoAttrs = this.meta.geoAttributes;
			var attr = getGeoAttrByName(geoAttrs, attrName);
			if (attr) {
				geoAttrs.remove(attr);
			}
		};
		treeVisit.call(this, tableId, deleteAttr);
	};
	
	var onChangeVisibilityGeoattr = function(p) {
		var tableId = p.classId;
		var attrName = p.record.data.name;
		var cachedTable = CMDBuild.Cache.getTableById(tableId);
		var geoAttrs = cachedTable.meta.geoAttributes;
		var attr = getGeoAttrByName(geoAttrs, attrName);
		if (attr) {
			attr.isvisible = p.checked;
		} else {
			// the table does not have the layer
			var dollyAttr = Ext.apply({}, p.record.data);
			geoAttrs.push(dollyAttr);
		}
	};
	
	CMDBuild.Administration.LayerController = Ext.extend(Ext.Component, {
		initComponent: function() {
			CMDBuild.Administration.LayerController.superclass.initComponent.call(this, arguments);
			this.subscribe("cmdb-new-geoattr", onNewGeoattr, this);
			this.subscribe("cmdb-modify-geoattr", onModifyGeoattr, this);
			this.subscribe("cmdb-delete-geoattr", onDeleteGeoattr, this);
			this.subscribe("cmdb-changevisibility-geoattr", onChangeVisibilityGeoattr, this);
		}
	});	
})();

CMDBuild.layerController = new CMDBuild.Administration.LayerController();