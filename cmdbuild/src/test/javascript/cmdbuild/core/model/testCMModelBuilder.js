(function() {
	TestCase("testCMModelBuilder", {
		setUp: function() {
			this.m = CMDBuild.core.model.CMModelBuilder;
		},
		tearDown: function() {
			delete this.m;
		},
		"test build fail without conf": function() {
			try {
				this.m.build();
				fail("build is not failed without conf");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.NO_CONFIGURATION, e);
			}
		},
		"test build fail without conf.name": function() {
			try {
				this.m.build({});
				fail("build is not failed without conf.name");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.NO_CONFIGURATION_NAME, e);
			}
		},
		"test build fail without conf.structure": function() {
			try {
				this.m.build({
					name: "foo"
				});
				fail("build is not failed without conf.structure");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.NO_CONFIGURATION_STRUCTURE, e);
			}
		},
		"test build fail if conf.structure is not an object": function() {
			try {
				this.m.build({
					name: "foo",
					structure: "foo"
				});
				fail("build is not failed but conf.structure is not an object");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.CONFIGURATION_STRUCTURE_IS_NOT_OBJECT, e);
			}
		},
		"test new model has a NAME constant with his name": function() {
			var name = "Foo";
			var Model = this.m.build({
				name: name,
				structure: []
			});
			assertEquals(Model.NAME, name);
		},
		"test new model has a NAME constant with his name": function() {
			var structure = {
				asdf: {}
			};
			var Model = this.m.build({
				name: "Foo",
				structure: structure 
			});
			assertSame(Model.STRUCTURE, structure);
		},
		"test an instance of new model knows his name": function() {
			var name = "Foo";
			var M = this.m.build({
				name: name,
				structure: {}
			});
			
			var m = new M();
			assertEquals(m.NAME, name);
		},
		"test an instance of new model knows his structure": function() {
			var structure = {
				foo: {}
			};
			
			var M = this.m.build({
				name: "Foo",
				structure: structure
			});
			
			var m = new M();
			assertSame(m.STRUCTURE, structure);
		},
		"test fail to instance a model without a required attr": function() {
			var structure = {
				requiredAttr: {
					required: true
				},
				notRequiredAttr: {}
			};

			var M = this.m.build({
				name: "Foo",
				structure: structure
			});
			
			try {
				var m = new M({});
				fail("Is istantiate a model without a required attribute");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.REQUIRED_ATTRIBUTE("requiredAttr"), e);
			}
		},
		"test instance.getRecord return a right record": function() {
			var M = getModel();
			
			var m = new M({
				attr1: "foo",
				attr2: "foo foo"
			});
			
			var r = m.getRecord();
			assertEquals("foo", r.get("attr1"));
			assertEquals("foo foo", r.get("attr2"));
		},
		"test instance.set fail for a wrong attribute": function() {
			var M = getModel();
			var m = new M({
				attr1: "foo",
				attr2: "foo foo"
			});
			
			try {
				m.set("Agamennone", "Caio");
				fail("Is setted an undefined attribute");
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.SET_UNDEFINED_ATTR, e);
			}
		},
		"test that instance.get return the right value": function() {
			var M = getModel();
			var value = "foo";
			var m = new M({
				attr1: value,
				attr2: "foo foo"
			});
			assertEquals(value, m.get("attr1"));
		},
		"test that instance.get return undefined if has not the attribute": function() {
			var m = getInstance();
			assertUndefined(m.get("foo"));
		},
		"test that instance.set sets well": function() {
			var m = getInstance();
			var value = "theValue";
			m.set("attr1", value);
			assertEquals(value, m.get("attr1"));
		},
		"test that instance.set fires the right event": function() {
			var m = getInstance();
			var spy = sinon.spy();
			m.on(m.CMEVENTS.CHANGED, spy);
			m.set("attr1", "asdf");
			assertTrue(spy.called);
		},
		"test generate setters": function() {
			var m = getInstance();
			assertNotUndefined(m.setattr1);
			assertNotUndefined(m.setattr2);
		},
		"test generated setters really sets the attr": function() {
			var m = getInstance();
			var newVal = "Agamennone";
			m.setattr1(newVal);
			assertEquals(newVal, m.get("attr1"));
		},
		"test generate getter": function() {
			var m = getInstance();
			assertNotUndefined(m.getattr1);
			assertNotUndefined(m.getattr2);
		},
		"test generated getter return right values": function() {
			var m = getInstance();
			assertEquals("foo", m.getattr1());
			m.setattr1("newValue");
			assertEquals("newValue", m.getattr1());
		},
		"test updete must have an obj as parameter": function() {
			var M1 = getModel("M1");
			var m1 = new M1();
			try {
				m1.update();
				fail ("Update has not throw exception not object parameter")
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.WRONG_UPDATE_PARAMETER, e)
			}
		},
		"test updete fail for not same model": function() {
			var M1 = getModel("M1");
			var M2 = getModel("M2");
			var m1 = new M1();
			var m2 = new M2();
			
			try {
				m1.update(m2);
				fail ("Update has not throw exception for wrong model type")
			} catch (e) {
				assertEquals(CMDBuild.core.error.model.WRONG_MODEL_TYPE, e)
			}
		},
		"test updete model": function() {
			var M = getModel("M");
			var ma = new M();
			var mb = new M();
			mb.setattr1("Agamennone");
			mb.setattr2("Ettore");
			ma.update(mb);
			assertEquals(mb.getattr1(), ma.getattr1());
			assertEquals(mb.getattr2(), ma.getattr2());
		},
		"test destroy fire event": function() {
			var m = getInstance();
			var spy = sinon.spy();
			m.on(m.CMEVENTS.DESTROY, spy);
			m.destroy();
			assertTrue(spy.called);
		}
	});
	
	function getModel(name) {
		return CMDBuild.core.model.CMModelBuilder.build({
			name: name || "Foo",
			structure: {
				attr1: {},
				attr2: {}
			}
		});
	}
	
	function getInstance() {
		var M = getModel();
		return new M({
			attr1: "foo",
			attr2: "foo foo"
		});
	}
})();