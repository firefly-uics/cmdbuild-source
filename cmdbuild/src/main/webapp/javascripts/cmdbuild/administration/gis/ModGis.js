(function() {
	var configUpdated = function() {
		this.treePanel.setDisabled(!CMDBuild.Config.gis.enabled);
    };
    
    CMDBuild.GisTreePanelController = Ext.extend(CMDBuild.TreePanelController, {
		initComponent : function() {
			CMDBuild.GisTreePanelController.superclass.initComponent.apply(this, arguments);
			this.listen('cmdb-config-update-gis', configUpdated);
		}
	});
    
    CMDBuild.Administration.GisTree = Ext.extend(CMDBuild.TreePanel, {
    	id: "gis_tree",
    	translation: CMDBuild.Translation.administration.modcartography,
    	title: CMDBuild.Translation.administration.modcartography.title,
    	initComponent: function() {
    		CMDBuild.Administration.GisTree.superclass.initComponent.apply(this, arguments);
    		this.root.appendChild(new Ext.tree.TreeNode({
    			text: this.translation.icons.title,
    			allowDrag: false,
    			selectable: true,
    			type: "gis-icons"
    		}));
    		this.root.appendChild(new Ext.tree.TreeNode({
    			text: this.translation.external_services.title,
    			allowDrag: false, 
    			selectable: true,
    			type: "gis-external-services"
    		}));
    		this.root.appendChild(new Ext.tree.TreeNode({
    			text: this.translation.layermanager.title,
    			allowDrag: false, 
    			selectable: true,
    			type: "gis-layers-order"
    		}));
    		this.root.appendChild(new Ext.tree.TreeNode({
    			text: this.translation.geoserver.title,
    			allowDrag: false, 
    			selectable: true,
    			type: "gis-geoserver"
    		}));
    		this.setDisabled(!CMDBuild.Config.gis.enabled);
    	}
    });
    
})();