(function() {
	TestCase("testCMAttributeModelLibrary", {
		setUp: function() {
			this.al = new CMDBuild.core.model.CMAttributeModelLibrary();
		},
		tearDown: function() {
			this.al.clear()
			delete this.al;
		},
		"test to add a CMAttribute": function() {
			this.al.add(getAnAttribute());
		},
		"test asStore return an object": function() {
			var store = this.al.asStore();
			assertObject(store);
			delete store;
		},
		"test given store has the same record of the library": function() {
			var attribute1 = getAnAttribute("a1");
			var attribute2 = getAnAttribute("a2");
			
			this.al.add(attribute1);
			this.al.add(attribute2);
			
			var store = this.al.asStore();
			
			assertEquals(2, store.getCount());
			delete store;
		},
		"test add to the library add also to the store": function() {
			var store = this.al.asStore();
			var attribute1 = getAnAttribute("a1");
			var attribute2 = getAnAttribute("a2");
			
			assertEquals(0, store.getCount())
			this.al.add(attribute1);
			assertEquals(1, store.getCount())
			this.al.add(attribute2);
			assertEquals(2, store.getCount());
			delete store;
		},
		"test remove to the library remove also to the store": function() {
			var store = this.al.asStore();
			var attribute1 = getAnAttribute("a1");
			var attribute2 = getAnAttribute("a2");
			
			assertEquals(0, store.getCount());
			this.al.add(attribute1);
			this.al.add(attribute2);
			assertEquals(2, store.getCount());
			this.al.remove(attribute1.getname());
			assertEquals(1, store.getCount());
			this.al.remove(attribute2.getname());
			assertEquals(0, store.getCount());
			delete store;
		}
	});
	
	function getAnAttribute(name) {
		var conf = {
			active: true,
			description: "Foo",
			name: name || "Foo",
			unique: true,
			shownAsGridColumn: false,
			editingMode: "write",
			type: "STRING",
			stringLength: 20,
			notnull: false
		}
	
		return new CMDBuild.core.model.CMAttributeModel(conf);
	}
})();