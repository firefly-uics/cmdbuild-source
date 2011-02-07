(function() {
	
var buildMenuTree = function() {
	// the menu tree is the tree of groups with an added node that represents the
	// default group. To build this tree take the groups from the cache
	// add a the fake group and build a tree
	var ct = CMDBuild.Constants.cachedTableType;
	var groups = CMDBuild.Cache.getTablesByGroup(ct.group) || {};
	var menutables = Ext.apply({
		0: {
		    text: "* Default *", // administration.modsecurity.defaultgroup
		    id: "0",
		    type: "menu",
		    selectable: true
		}
	}, groups);
	
	return CMDBuild.TreeUtility.buildTree(menutables, "menu", undefined, sorted=true);
};

CMDBuild.Administration.MenuTree = Ext.extend(CMDBuild.TreePanel, {
	title: CMDBuild.Translation.administration.modmenu.custom_menus, 
	initComponent: function() {
		this.root = buildMenuTree();
		this.rootVisible = false;
		CMDBuild.Administration.MenuTree.superclass.initComponent.apply(this, arguments);
	}
});

CMDBuild.MenuTreePanelController = Ext.extend(CMDBuild.TreePanelController, {
	silentListener: true,
	initComponent: function() {
		CMDBuild.MenuTreePanelController.superclass.initComponent.apply(this, arguments);	
	},
	onSelectNode: function(node) {
		if (node) {
			var attributes = CMDBuild.Cache.getTableById(node.id) || node.attributes;
			if (attributes && attributes.selectable) {
				this.publish("cmdb-select-menu", attributes);
			}
		} 
	}
});

CMDBuild.Administration.ModMenu = Ext.extend(CMDBuild.ModPanel, {
	id : 'menu_panel',
	modtype: 'menu',
	title : CMDBuild.Translation.administration.modmenu.title,
	basetitle : CMDBuild.Translation.administration.modmenu.title+ ' - ',
	layout: 'fit',

    initComponent: function() {
		var mp = new CMDBuild.Administration.MenuPanel();
    	Ext.apply(this, {
    		frame: false,
    		border: true,
    		items: [mp]
    	});
    	CMDBuild.Administration.ModMenu.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-select-menu', this.selectMenu, this);
    	this.on("show", mp.recalculateTreesHeight, mp);
   	},

	selectMenu: function(eventParams) {
		if (eventParams) {
			this.publish('cmdb-init-menu', {
					groupId: eventParams.id
			});
		}
	}
});
})();