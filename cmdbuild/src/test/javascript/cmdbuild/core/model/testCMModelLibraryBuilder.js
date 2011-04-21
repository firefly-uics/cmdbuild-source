(function() {
	TestCase("testCMModelLibraryBuilder", {
		setUp: function() {
			this.ml = CMDBuild.core.model.CMModelLibraryBuilder;
		},
		tearDown: function() {
			delete this.ml;
		},
		"test build fails without conf": function() {
			try {
				this.ml.build();
				fail("A library was build without conf");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.NO_CONFIGURATION, e);
			}
		},
		"test build fails without model name": function() {
			try {
				this.ml.build({});
				fail("A library was build without model name");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.NO_MODEL_NAME, e);
			}
		},		
		"test build fails if name is not a string": function() {
			try {
				this.ml.build({
					modelName:[]
				});
				fail("A library was build but the model name is not a string");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.MODEL_NAME_IS_NOT_STRING, e);
			}
		},
		"test build fails without keyAttribute": function() {
			try {
				this.ml.build({
					modelName: "Foo"
				});
				fail("A library was build without key attribute name");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.NO_KEY_ATTRIBUTE, e);
			}
		},
		"test get fails without an id": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			try {
				fooLibrary.get();
				fail("Get without id is not failed");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.GET_WITHOUT_ID, e)
			}
		},
		"test get returns undefined for new library": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			assertUndefined(fooLibrary.get("asdf"));
		},
		"test add fails without model": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			try {
				fooLibrary.add();
				fail("Add is not failed without model");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.A_MODEL_IS_REQUIRED, e);
			}
		},
		"test add fails for wrong model type": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var stubModel = getModelStub(name="asdf");
			try {
				fooLibrary.add(stubModel);
				fail("A wrong model is added to a model library");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.ADD_WRONG_MODEL_TO_LIBRARY, e);
			}
		},
		"test get return the added model": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var stubModel = getModelStub(name="Foo", id=001);
			fooLibrary.add(stubModel);
			assertSame(stubModel, fooLibrary.get(001));
		},
		"test add 3 models": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var stubModel1 = getModelStub(name="Foo", id=001);
			var stubModel2 = getModelStub(name="Foo", id=002);
			var stubModel3 = getModelStub(name="Foo", id=003);
			
			fooLibrary.add(stubModel1);
			fooLibrary.add(stubModel2);
			fooLibrary.add(stubModel3);
			assertSame(stubModel1, fooLibrary.get(001));
			assertSame(stubModel2, fooLibrary.get(002));
			assertSame(stubModel3, fooLibrary.get(003));
		},
		"test add fire events": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var m = getModelStub(name="Foo", id=001);
			var spy = sinon.spy();
			fooLibrary.on(FooLibrary.CMEVENTS.ADD, spy);
			fooLibrary.add(m);
			assertTrue(spy.called);
		},
		"test remove fails without id": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			try {
				fooLibrary.remove();
				fail("Remove is not failed without id");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.REMOVE_WITHOUT_ID, e);
			}
		},
		"test remove really removes": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var id = "theId";
			var m = getModelStub(name="Foo", id);
			assertUndefined(fooLibrary.get(id));
			fooLibrary.add(m);
			assertEquals(m, fooLibrary.get(id));
			fooLibrary.remove(id);
			assertUndefined(fooLibrary.get(id));
		},
		"test remover fire events": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var m = getModelStub(name="Foo", id=001);
			var spy = sinon.spy();
			fooLibrary.on(FooLibrary.CMEVENTS.REMOVE, spy);
			fooLibrary.add(m);
			fooLibrary.remove(m.getid());
			assertTrue(spy.called);
		},
		"test count": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var m1 = getModelStub(name="Foo", id=001);
			var m2 = getModelStub(name="Foo", id=002);
			var m3 = getModelStub(name="Foo", id=003);
			
			assertEquals(0, fooLibrary.count());
			fooLibrary.add(m1);
			assertEquals(1, fooLibrary.count());
			fooLibrary.add(m2);
			assertEquals(2, fooLibrary.count());
			fooLibrary.add(m3);
			assertEquals(3, fooLibrary.count());
		},
		"test clear": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			var m1 = getModelStub(name="Foo", id=001);
			var m2 = getModelStub(name="Foo", id=002);
			var m3 = getModelStub(name="Foo", id=003);
			
			assertEquals(0, fooLibrary.count());
			fooLibrary.add(m1);
			fooLibrary.add(m2);
			fooLibrary.add(m3);
			assertEquals(3, fooLibrary.count());
			fooLibrary.clear();
			assertEquals(0, fooLibrary.count());
		},
		"test hasModel": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "id"});
			var fooLibrary = new FooLibrary();
			assertFalse(fooLibrary.hasModel("Agamennone"));
			var m1 = getModelStub(name="Foo", id=001);
			fooLibrary.add(m1);
			assertTrue(fooLibrary.hasModel(001));
		},
		"test remove model on model destroy": function() {
			var FooLibrary = this.ml.build({modelName:"Foo", keyAttribute: "attr1"});
			var fooLibrary = new FooLibrary();
			var Foo = getModel();
			var f = new Foo({
				attr1: 001
			});
			fooLibrary.add(f);
			assertTrue(fooLibrary.hasModel(001));
			f.destroy();
			assertFalse(fooLibrary.hasModel(001));
		}
	});
	
	function getModelStub(name, id) {
		var model = {
			NAME: name,
			CMEVENTS: {
				DESTROY: "destroy"
			},
			on: function() {},
			getid: sinon.stub().returns(id)
		};
		return model;
	}
	
	function getModel(name) {
		return CMDBuild.core.model.CMModelBuilder.build({
			name: name || "Foo",
			structure: {
				attr1: {},
				attr2: {}
			}
		});
	}
})();