(function() {

	var groups = {};

	Ext.define("CMDBUild.cache.CMCacheGroupsFunctions", {
		addGroups: function(etypes) {
			for (var i=0, l=etypes.length; i<l; ++i) {
				this.addGroup(etypes[i]);
			}
		},

		addGroup: function(g) {
			var group = Ext.create("CMDBuild.cache.CMGroupModel", g);
			groups[g.id] = group;
			
			return group;
		},
		
		getGroups: function() {
			return groups;
		},
		
		getGroupById: function(id) {
			return groups[id] || null;
		},
		
		onGroupSaved: function(group) {
			var g = this.addGroup(group);

			this.fireEvent("cm_group_saved", g);
		}
	});
})();