(function() {
	Ext.define("CMDBuild.view.administration.widget.form.DummyWidgetForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",
		statics: {
			WIDGET_NAME: "DUMMY"
		},
		buildForm: function() {},
		getWidgetDefinition: function() {}
	});

	CMDBuild.Translation.administration.modClass.widgets["DUMMY"] = {
		title: "Dummy"
	};

	TestCase("testCMWidgetDefinitionPanel", {

		setUp: function() {
			this.panel = new CMDBuild.view.administration.widget.CMWidgetDefinitionPanel();
		},

		tearDown: function() {
			delete this.panel;
		},

		"test initial situation": function() {
			assertTrue(this.panel.saveButton.disabled);
			assertTrue(this.panel.abortButton.disabled);
			assertFalse(this.panel.addButton.disabled);
		},

		"test buildWidgetForm throw an exception if the widget name is not found": function() {
			var name = "sbirigudda";

			try {
				this.panel.buildWidgetForm(name);
				fail("buildWidgetForm for a wrong name must throw an exception");
			} catch (e) {
				assertEquals(this.panel.EXCEPTIONS.notAWidget(name), e);
			}
		},

		"test buildWidgetForm return the form if the name exists": function() {
			var form = this.panel.buildWidgetForm("DUMMY");
			assertObject(form);
			assertEquals("CMDBuild.view.administration.widget.form.DummyWidgetForm", form.$className);
		},

		"test addRecordToGrid throw an error if the paramether is not the right model": function() {
			var m = {};

			try {
				this.panel.addRecordToGrid(m);
				fail("the add of a wrong object must fail");
			} catch (e) {
				assertEquals(this.panel.EXCEPTIONS.notAWidgetModel(m), e);
			}
		},

		"test addRecordToGrid call addRecord of his grid": function() {
			var addRecord = sinon.spy(this.panel.grid, "addRecord");
			var r = new CMDBuild.model.CMWidgetDefinitionModel();

			this.panel.addRecordToGrid(r);
			assertTrue(addRecord.calledWith(r));

			addRecord.restore();
		},

		"test removeRecordFromGrid call remove of his grid": function() {
			var removeRecordWithId = sinon.spy(this.panel.grid, "removeRecordWithId");
			var recordId = "foo";

			this.panel.removeRecordFromGrid(recordId);
			assertTrue(removeRecordWithId.calledWith(recordId));

			removeRecordWithId.restore();
		},

		"test disable modify disable all the components": function() {
			var disableModify = sinon.mock(this.panel.form).expects("disableModify").once();

			this.panel.disableModify();
			assertTrue("save button is enabled", this.panel.saveButton.disabled);
			assertTrue("abort button is enabled", this.panel.abortButton.disabled);

			disableModify.verify();
		},

		"test disable modify forward his params to form.disableModify": function() {
			var mock = sinon.mock(this.panel.form);
			mock.expects("disableModify").once().withArgs(true);
			this.panel.disableModify(true);

			mock.expects("disableModify").once().withArgs(false);
			this.panel.disableModify(false);

			mock.expects("disableModify").once().withArgs(undefined);
			this.panel.disableModify();
			mock.verify();
		},

		"test enable modify the call to the form and disable the button": function() {
			var mock = sinon.mock(this.panel.form);
			mock.expects("enableModify").once();
			this.panel.enableModify();
			assertFalse("save button is disabled", this.panel.saveButton.disabled);
			assertFalse("abort button is disabled", this.panel.abortButton.disabled);

			mock.verify();
		},

		"test that reset deselect the grid": function() {
			var mock = sinon.mock(this.panel.grid).expects("clearSelection").once();

			this.panel.reset();
			mock.verify();
		},

		"test that reset forward the call to form and disable modify": function() {
			var reset = sinon.mock(this.panel.form).expects("reset").once();
			var disableModify = sinon.mock(this.panel).expects("disableModify").once().withArgs(false);

			this.panel.reset();
			reset.verify();
			disableModify.verify();
		},

		"test getWidgetDefinition call the subpanel": function() {
			var form = this.panel.buildWidgetForm("DUMMY");
			var getWidgetDefinition = sinon.stub(form, "getWidgetDefinition").returns({type: "DUMMY", buttonLabel: "Olimpia"});

			var def = this.panel.getWidgetDefinition();
			assertTrue(getWidgetDefinition.called);
			assertEquals("DUMMY", def.type);
			assertEquals("Olimpia", def.buttonLabel);

			getWidgetDefinition.restore();
		}
	});
})();