(function() {
	var constants = CMDBuild.Constants;
	
	var lookupTypeStore = getFakeStore()
	var lookupTypeStoreOnlyLeves = getFakeStore();

	
	var lookupTypes = {};

	Ext.define("CMDBUild.cache.CMCacheLookupFunctions", {
		getLookupTypes: function() {
			return lookupTypes;
		},

		addLookupTypes: function(ltypes) {
			for (var i=0, len=ltypes.length; i<len; ++i) {
				this.addLookupType(ltypes[i]);
			}
		},

		addLookupType: function(lt) {
			lookupTypes[lt.id] = Ext.create("CMDBuild.cache.CMLookupTypeModel", lt);
		},

		getLookupTypeAsStore: function() {
			if (lookupTypeStore.cmFake) {
				lookupTypeStore = buildLookupTypeStore(onlyLeaves = false);
				lookupTypeStore.cmFill();
			}
			
			return lookupTypeStore;
		},

		getLookupTypeLeavesAsStore: function() {
			if (lookupTypeStoreOnlyLeves.cmFake) {
				lookupTypeStoreOnlyLeves = buildLookupTypeStore(onlyLeaves = true);
				lookupTypeStoreOnlyLeves.cmFill();
			}
	
			return lookupTypeStoreOnlyLeves;
		},

		onNewLookupType: function(lType) {
			this.addLookupType(lType);
			lookupTypeStore.cmFill()
			lookupTypeStoreOnlyLeves.cmFill();
			this.fireEvent("cm_new_lookuptype", lType);
		},

		onModifyLookupType: function(lType) {
			if (lType.oldId) {
				lookupTypes[lType.oldId] = undefined;
				delete lookupTypes[lType.oldId];
				this.addLookupType(lType)
			} else {
				var lt = lookupTypes(lType.id);
				lt.set(lType);
			}

			this.syncLookupTypesParent(lType);
			lookupTypeStore.cmFill()
			lookupTypeStoreOnlyLeves.cmFill();
			this.fireEvent("cm_modified_lookuptype", lType);
		},

		syncLookupTypesParent: function(lType) {
			for (var i in lookupTypes) {
				var lt = lookupTypes[i];

				if (lt.get("parent") == lType.oldId) {
					lt.set("parent", lType.id);
				}
			}
		}
		
	});

	function buildLookupTypeStore(onlyLeaves) {
		var store = new Ext.data.Store({
			model: "CMLookupTypeForCombo",
			cmOnlyLeaves: onlyLeaves,
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in lookupTypes) {
					var lt = lookupTypes[i];
					if (!this.cmOnlyLeaves || (this.cmOnlyLeaves && !lt.children)) {
						data.push([lt.get("id")]);
					}
				}
				this.add(data);
				this.sort("type", "ASC");
			},
			sorters: [{
				property : 'type',
				direction : 'ASC'
			}]
		});

		return store;
	}

	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}

})();