(function() {

	var classes = {},
		processes = {},
		geoAttributesSotores = {},
		superclassesStore = getFakeStore(),
		superProcessStore = getFakeStore(),
		classStore = getFakeStore(),
		classesAndProcessesStore = getFakeStore();

	Ext.define("CMDBUild.cache.CMCacheClassFunctions", {

		getClasses: function() {
			return classes;
		},

		getProcesses: function() {
			return processes;
		},

		addClasses: function(etypes) {
			for (var i=0, l=etypes.length; i<l; ++i) {
				var et = etypes[i];
				if (et.type == "class") {
					this.addClass(etypes[i]);
				} else if(et.type == "processclass") {
					this.addProcess(etypes[i]);
				}
			}

			callCmFillForStores();
		},

		addClass: function(et) {
			var newEt = Ext.create("CMDBuild.cache.CMEntryTypeModel", et);
			classes[et.id] = newEt;
			
			return newEt;
		},

		addProcess: function(et) {
			var newEt = Ext.create("CMDBuild.cache.CMEntryTypeModel", et);
			processes[et.id] = newEt;
			
			return newEt;
		},
		
		getClassById: function(id) {
			return classes[id];
		},
		
		getProcessById: function(id) {
			return processes[id];
		},

		getSuperclassesAsStore: function() {
			if (superclassesStore.cmFake) {
				superclassesStore = buildSuperclassesStore();
				superclassesStore.cmFill();
			}

			return superclassesStore;
		},
		
		getSuperProcessAsStore: function() {
			if (superProcessStore.cmFake) {
				superProcessStore = buildSuperProcessStore();
				superProcessStore.cmFill();
			}

			return superProcessStore;
		},
		
		getClassesStore: function() {
			if (classStore.cmFake) {
				classStore = buildClassesStore();
				classStore.cmFill();
			}

			return classStore;
		},
		
		getClassesAndProcessesStore: function() {
			if (classesAndProcessesStore.cmFake) {
				classesAndProcessesStore = buildClassesAndProcessesStore();
				classesAndProcessesStore.cmFill();
			}

			return classesAndProcessesStore;
		},

		getGeoAttributesStoreOfClass: function(classId) {
			if (!geoAttributesSotores[classId]) {
				geoAttributesSotores[classId] = buildGeoAttributesStoreForClass(classId);
			}

			return geoAttributesSotores[classId];
		},
		
		onClassSaved: function(class) {
			var c = this.addClass(class);
			callCmFillForStores();
			this.fireEvent("cm_class_saved", c);

			return c;
		},
		
		onProcessSaved: function(process) {
			var p = this.addProcess(process);
			callCmFillForStores();
			this.fireEvent("cm_process_saved", p);

			return p;
		},

		onClassDeleted: function(idClass) {
			classes[idClass] = undefined;
			delete classes[idClass];
			callCmFillForStores();
			
			this.fireEvent("cm_class_deleted", idClass);
		},

		onProcessDeleted: function(idClass) {
			processes[idClass] = undefined;
			delete processes[idClass];
			callCmFillForStores();
			
			this.fireEvent("cm_process_deleted", idClass);
		},

		getClassRootId: function() {
			return getTableIdFromSetByName(classes, "Class");
		},
		
		getActivityRootId: function() {
			return getTableIdFromSetByName(processes, "Activity");
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
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
		return store;
	}

	function buildClassesStore() {
		return new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in classes) {
					var table = classes[i];
					if (table.data.name != "Class") {
						data.push({
							id: table.data.id,
							description: table.data.text
						});
					}
				}
				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
	}
	
	function buildClassesAndProcessesStore() {
		return new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				
				function addToData(t) {
					if (t.data.name != "Class" && t.data.name != "Activity") {
						data.push({
							id: t.data.id,
							description: t.data.text
						});
					}
				}

				for (var i in classes) {
					addToData(classes[i]);
				}
				for (var i in processes) {
					addToData(processes[i]);
				}

				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
	}
	
	function buildSuperProcessStore() {
		return new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in processes) {
					var table = processes[i];
					if (table.data.superclass) {
						data.push({
							id: table.data.id,
							description: table.data.text
						});
					}
				}
				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
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
	
	function getTableIdFromSetByName(set, name) {
		for (var t in set) {
			t = set[t]; 
			if (t.get("name") == name) {
				return t.get("id");
			}
		}
	}
	
	function callCmFillForStores() {
		classStore.cmFill();
		superProcessStore.cmFill();
		superclassesStore.cmFill();
		classesAndProcessesStore.cmFill();
	}
})();