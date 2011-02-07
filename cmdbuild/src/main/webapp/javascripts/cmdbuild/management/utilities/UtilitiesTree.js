CMDBuild.Management.UtilitiesTree = Ext.extend(CMDBuild.TreePanel, {
	id: 'utilities_tree',
	submodules: {},
	dataUrl: '',
	initComponent: function(){
		CMDBuild.Management.UtilitiesTree.superclass.initComponent.apply(this, arguments);
		for (var moduleName in this.submodules) {
			var module = this.submodules[moduleName];
			if (this.submoduleIsEnabled(moduleName)) {
				this.root.appendChild(new Ext.tree.TreeNode({
					text: module.title,
					type: module.type,
					selectable: true,
					allowDrag: false
				}));
			}
		}
	},

	submoduleIsEnabled: function(moduleName) {
		if (moduleName == 'changePassword' && !CMDBuild.Runtime.CanChangePassword) {
			return false;
		} else {
			return !CMDBuild.Runtime.DisabledModules[moduleName];
		}
	}
});
