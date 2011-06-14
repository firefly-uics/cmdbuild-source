(function() {

	var classes = {};
	var geoAttributesSotores = {};
	var superclassesStore = getFakeStore();
	var classStore = getFakeStore();

	Ext.define("CMDBUild.cache.CMCacheClassFunctions", {

		getClasses: function() {
			return classes;
		},

		addClasses: function(etypes) {
			for (var i=0, l=etypes.length; i<l; ++i) {
				var et = etypes[i];
				if (et.type == "class") {
					this.addClass(etypes[i]);
				}
			}
		},

		addClass: function(et) {
			var newEt = Ext.create("CMDBuild.cache.CMEntryTypeModel", et);
			classes[et.id] = newEt;
			
			return newEt;
		},

		getClassById: function(id) {
			return classes[id];
		},
		
		getSuperclassesAsStore: function() {
			if (superclassesStore.cmFake) {
				superclassesStore = buildSuperclassesStore();
				superclassesStore.cmFill();
			}

			return superclassesStore;
		},
		
		getClassesStore: function() {
			if (classStore.cmFake) {
				classStore = buildClassesStore();
				classStore.cmFill();
			}

			return classStore;
		},
		
		getClassRootId: function() {
			for (var c in classes) {
				c = classes[c];
				if (c.get("name") == "Class") {
					return c.get("id");
				}
			}
		},
		
		getGeoAttributesStoreOfClass: function(classId) {
			if (!geoAttributesSotores[classId]) {
				geoAttributesSotores[classId] = buildGeoAttributesStoreForClass(classId);
			}

			return geoAttributesSotores[classId];
		},
		
		onClassSaved: function(class) {
			var c = this.addClass(class);
			superclassesStore.cmFill();
			this.fireEvent("cm_class_saved", c);

			return c;
		},
		
		onClassDeleted: function(idClass) {
			classes[idClass] = undefined;
			delete classes[idClass];
			superclassesStore.cmFill();
			this.fireEvent("cm_class_deleted", idClass);
		},
		
		onGeoAttributeSaved: function(idClass, geoAttribute) {
			var s = this.getGeoAttributesStoreOfClass(idClass);
			if (s) {
				var r = s.findRecord("name", geoAttribute.name);
				if (r) {
					s.remove(r)
				}
				
				s.add(geoAttribute);
				s.sort('index', 'ASC');
			}
		}

	});

	function buildSuperclassesStore() {
		var store = new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in classes) {
					var table = classes[i];
					if (table.data.superclass) {
						data.push({
							id: table.data.id,
							description: table.data.text
						});
					}
				}
				this.add(data);
			},
			sorters: [{
				property : 'text',
				direction : 'ASC'
			}]
		});
		return store;
	}

	function buildClassesStore() {
		var store = new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in classes) {
					var table = classes[i];
					data.push({
						id: table.data.id,
						description: table.data.text
					});
				}
				this.add(data);
			},
			sorters: [{
				property : 'text',
				direction : 'ASC'
			}]
		});
		return store;
	}

	// returns a null object (pattern) to avoid checks on onClassSaved
	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}
	
	function buildGeoAttributesStoreForClass(classId) {
		try {
			var geoAttributes = classes[classId].data.meta.geoAttributes;
			
			var s = new Ext.data.Store({
				fields: [
					"index", "name", "description", "type",
					"isvisible", "masterTableId", "masterTableName",
					"minZoom", "maxZoom", "style"
				],
				autoLoad : false,
				data: geoAttributes || [],
				sorters : [ {
					property : 'index',
					direction : "ASC"
				}]
			});
	
			return s;
		} catch (e) {
			_debug("I can not build a geoAttribute store for classId with id " + classId);
		}
	}
})();