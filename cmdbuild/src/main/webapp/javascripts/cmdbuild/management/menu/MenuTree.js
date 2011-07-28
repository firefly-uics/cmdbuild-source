CMDBuild.Management.MenuTree = Ext.extend(CMDBuild.TreePanel, {
	hidden: false,
	disableSort: true,
	initComponent: function() {
		this.root = CMDBuild.Cache.menuTree,
		this.rootVisible = false;
		this.border = false;
		CMDBuild.Management.MenuTree.superclass.initComponent.apply(this, arguments);	
	}
});