(function() {

	Ext.define("CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController", {
		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: "DUMMY"
		},

		getDataToSubmit: function() {
			return {
				"Ettore": true,
				"Agamennone": true,
				"Achille": true
			};
		},

		fillFormWithModel: function(model) {}
	});

	Ext.define("CMDBuild.view.administration.widget.form.DummyWidgetForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: "DUMMY"
		},

		buildForm: function() {}
	});

	Ext.define("CMDBuild.view.administration.widget.CMWidgetDefinitionDummyPanel", {
		extend: "CMDBuild.view.administration.widget.CMWidgetDefinitionPanelInterface",
		buildWidgetForm: function() {
			return new CMDBuild.view.administration.widget.form.DummyWidgetForm();
		}
	});

	TestCase("testCMWidgetDefinitionController", {

		setUp: function() {
			this.spies = [];
			this.server = CMDBuild.test.CMServer.create();
			this.view = new CMDBuild.view.administration.widget.CMWidgetDefinitionDummyPanel();
			this.mockView = sinon.mock(this.view);
			this.wdc = new CMDBuild.controller.administration.widget.CMWidgetDefinitionController(this.view);
		},

		tearDown: function() {
			this.server.restore();
			delete this.server;
			delete this.view;
			delete this.mockView;
			delete this.wdc;
		},

		/********** CLASS SELECTED */

		"test call reset of view on classSelected": function() {
			var reset = sinon.spy(this.view, "reset");
			var getClassById = sinon.stub(_CMCache, "getClassById");
			getClassById.returns({ get: function() {return "asdf";}});

			this.wdc.onClassSelected(1);

			assertTrue(reset.calledWith(removeAllRecords = true));

			reset.restore();
			getClassById.restore();
		},

		"test load widgets on classSelected": function() {
			var classId = 11, className = "Foo";
			var getClassById = sinon.stub(_CMCache, "getClassById");
			var read = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "read");

			getClassById.returns({ get: function() {return className;}});

			this.wdc.onClassSelected(classId);

			assertTrue(read.called);
			var arg = read.getCall(0).args[0];
			assertEquals(className, arg.params.className);

			read.restore();
			getClassById.restore();
		},

		"test load widgets success call addRecordToGrid for the result": function() {
			var getClassById = sinon.stub(_CMCache, "getClassById");
			getClassById.returns({ get: function() {return "Foo";}});

			var addRecordToGrid = sinon.spy(this.view, "addRecordToGrid");

			this.server.bindUrl(CMDBuild.ServiceProxy.url.CMWidgetConfiguration.read, function() {
				return {
					success: true,
					widgets: [{
						id: 1,
						def: {
							type: "REPORT",
							buttonLabel: "Pirla",
							active: true
						}
					},{
						id: 2,
						def: {
							type: "REPORT",
							buttonLabel: "Callisto",
							active: true
						}
					}]
				};
			});

			this.wdc.onClassSelected();
			assertTrue(addRecordToGrid.calledTwice);
			getClassById.restore();
			addRecordToGrid.restore();
		},

		/********** ADD NEW */

		"test add subcontroller on addClick": function() {
			assertUndefined(this.wdc.subController);
			this._fireAddEvent();
			assertObject(this.wdc.subController);
			assertEquals("DUMMY", this.wdc.subController.WIDGET_NAME);
		},

		"test on AddClick call the view to have the subview": function() {
			var buildWidgetForm = sinon.spy(this.view, "buildWidgetForm");
			this._fireAddEvent();

			assertTrue(buildWidgetForm.called);
			var widgetName = buildWidgetForm.getCall(0).args[0];
			assertEquals("DUMMY", widgetName);

			buildWidgetForm.restore();
		},

		"test on AddClick reset the view if there is the subcontroller class and does not enable the editing": function() {
			var reset = sinon.spy(this.view, "reset");
			var enableModify = sinon.spy(this.view, "enableModify");
			this._fireAddEvent("Sbirigudda");

			assertTrue(reset.called);
			assertFalse(enableModify.called);
			reset.restore();
			enableModify.restore();
		},

		"test on AddClick enable the editing and call setDefaultValues of subcontroller": function() {
			var enableModify = sinon.spy(this.view, "enableModify");
			this._fireAddEvent();
			assertTrue(enableModify.called);
			enableModify.restore();
		},

		/********** SELECTION */

		"test select add a subcontroller and pass it the model": function() {
			assertUndefined(this.wdc.subController);
			this._fireSelectionEvent();
			assertObject(this.wdc.subController);
			assertEquals("DUMMY", this.wdc.subController.WIDGET_NAME);
		},

		"test selection disable the editing": function() {
			var mokedView = sinon.mock(this.view);
			mokedView.expects("disableModify").once();

			this._fireSelectionEvent();
			mokedView.verify();
		},

		"test manage model on grid (de)selection": function() {
			assertUndefined(this.wdc.model);
			this._fireSelectionEvent();
			assertObject(this.wdc.model);
			this._fireDeselectionEvent(this.wdc.model);
			assertUndefined(this.wdc.model);
		},

		"test deselect delete subcontroller ": function() {
			this._fireSelectionEvent();
			assertObject(this.wdc.subController);
			assertEquals("DUMMY", this.wdc.subController.WIDGET_NAME);
			this._fireDeselectionEvent(this.wdc.model);
			assertUndefined(this.wdc.subController);
		},

		/********** SAVE */

		"test save take the data from view": function() {
			var getClassById = sinon.stub(_CMCache, "getClassById").returns({ get: function() {return "Foo";}});
			var getWidgetDefinition = sinon.stub(this.view, "getWidgetDefinition").returns({ type: "DUMMY", buttonLabel: "Callisto"});
			var save = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "save");

			this._bindSaveSuccess();
			this._fireSelectionEvent();
			this._fireSaveEvent();

			assertTrue(getWidgetDefinition.called);
			assertTrue(save.called);

			var definition = this.view.getWidgetDefinition();
			var arg = save.getCall(0).args[0];
			assertEquals("Foo", arg.params.className);
			assertEquals(Ext.JSON.encode(definition), arg.params.definition);

			save.restore();
			getWidgetDefinition.restore();
			getClassById.restore();
		},

		"test save does not call the service proxy if has no subcontroller": function() {
			var save = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "save");

			this._bindSaveSuccess();
			this._fireSaveEvent();

			assertFalse(save.called);
			save.restore();
		},

		"test save call the service proxy without id to add a new widget": function() {
			var classId = "foo";
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});

			var save = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "save");

			this._bindSaveSuccess();
			this._fireAddEvent();
			this._fireSaveEvent();

			assertTrue(stub.called);
			assertTrue(save.called);
			var arg = save.getCall(0).args[0];
			assertUndefined(arg.params.id);

			save.restore();
			stub.restore();
		},

		"test save call the service proxy with id to modify a model": function() {
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});
			var save = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "save");

			this._bindSaveSuccess();
			this._fireSelectionEvent();
			this._fireSaveEvent();

			assertTrue(save.called);
			var arg = save.getCall(0).args[0];
			assertEquals("asdf", arg.params.id);

			save.restore();
			stub.restore();
		},

		"test save call the service proxy with className": function() {
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});
			var save = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "save");

			this._bindSaveSuccess();
			this._fireSelectionEvent();
			this._fireSaveEvent();

			var arg = save.getCall(0).args[0];
			assertEquals("Foo", arg.params.className);

			save.restore();
			stub.restore();
		},

		"test save success call right view methods": function() {
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});

			this._bindSaveSuccess();
			this._fireSelectionEvent();

			var mokedView = sinon.mock(this.view);
			mokedView.expects("addRecordToGrid").once();
			mokedView.expects("disableModify").once();

			this._fireSaveEvent();
			this.server.respond();
			mokedView.verify();
			stub.restore();
		},

		/********** REMOVE */

		"test remove does not call the service proxy if nothing is selected": function() {
			var remove = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "remove");
			this._fireRemoveEvent();
			assertFalse(remove.called);
			remove.restore();
		},

		"test remove call the service proxy with the model id and cassname as param": function() {
			var classId = "foo";
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});

			var remove = sinon.spy(CMDBuild.ServiceProxy.CMWidgetConfiguration, "remove");

			this._fireSelectionEvent();
			this._fireRemoveEvent();

			assertTrue(remove.called);
			var arg = remove.getCall(0).args[0];
			assertObject(arg);
			assertEquals("asdf", arg.params.id);
			assertEquals("Foo", arg.params.className);

			remove.restore();
			stub.restore();
		},

		"test remove call the right method of the view on success": function() {
			var classId = "foo";
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});

			var removeRecordFromGrid = sinon.spy(this.view, "removeRecordFromGrid");
			var reset = sinon.spy(this.view, "reset");

			this._bindRemoveSuccess();
			this._fireSelectionEvent();
			this._fireRemoveEvent();

			this.server.respond();

			assertTrue(removeRecordFromGrid.called);
			var id = removeRecordFromGrid.getCall(0).args[0];
			assertString(id);
			assertEquals("asdf", id);

			assertTrue(reset.called);

			removeRecordFromGrid.restore();
			reset.restore();
			stub.restore();
		},

		"test controller delete model and subcontroller on remove success": function() {
			var classId = "foo";
			var stub = sinon.stub(_CMCache, "getClassById");
			stub.returns({ get: function() {return "Foo";}});

			this._bindRemoveSuccess();
			this._fireSelectionEvent();

			assertObject(this.wdc.model);
			assertObject(this.wdc.subController);
			this._fireRemoveEvent();
			this.server.respond();

			assertUndefined(this.wdc.model);
			assertUndefined(this.wdc.subController);
			stub.restore();
		},

		/********** ABORT */

		"test click to abort after add cause view reset": function() {
			var reset = sinon.spy(this.view, "reset");

			this._fireAddEvent();
			this._fireAbortEvent();

			assertTrue(reset.called);

			reset.restore();
		},

		"test click to abort after modify re-select the model and disable modify": function() {
			var reset = sinon.spy(this.view, "reset");
			var disableModify = sinon.spy(this.view, "disableModify");

			this._addSubView();
			this._fireSelectionEvent();
			assertObject(this.wdc.model);

			var fillFormWithModel = sinon.spy(this.wdc.subController, "fillFormWithModel");

			this._fireAbortEvent();

			assertTrue(disableModify.calledWith(enableToolbar = true));
			assertFalse(reset.called);
			assertTrue(fillFormWithModel.called);
			var model = fillFormWithModel.getCall(0).args[0];
			assertEquals(this.wdc.model, model);

			reset.restore();
			disableModify.restore();
			fillFormWithModel.restore();
		},

		// support functions

		_addSubView: function() {
			this.subView = new CMDBuild.view.administration.widget.form.DummyWidgetForm();
			this.view.add(this.subView);
		},

		_bindSaveSuccess: function() {
			this.server.bindUrl(CMDBuild.ServiceProxy.url.CMWidgetConfiguration.save, function() {
				return {
					success: true,
					widget: getADummyWidgetConfiguration()
				};
			});
		},

		_bindRemoveSuccess: function() {
			this.server.bindUrl(CMDBuild.ServiceProxy.url.CMWidgetConfiguration.remove, function() {
				return {
					success: true
				};
			});
		},

		_fireSelectionEvent: function() {
			this.view.fireEvent("select", null, getADummyWidgetModel(), 1, {});
		},

		_fireDeselectionEvent: function(m) {
			this.view.fireEvent("deselect", null, m, 1, {});
		},

		_fireSaveEvent: function() {
			this.view.fireEvent("cm-save");
		},

		_fireRemoveEvent: function() {
			this.view.fireEvent("cm-remove");
		},

		_fireAddEvent: function(widgetName) {
			this.view.fireEvent("cm-add", widgetName || "DUMMY");
		},

		_fireAbortEvent: function() {
			this.view.fireEvent("cm-abort");
		}
	});

	function getADummyWidgetConfiguration() {
		var widget = {};
		widget[CMDBuild.model.CMWidgetDefinitionModel._FIELDS.id] = "asdf";
		widget[CMDBuild.model.CMWidgetDefinitionModel._FIELDS.type] = CMDBuild.view.administration.widget.form.DummyWidgetForm.WIDGET_NAME;
		widget[CMDBuild.model.CMWidgetDefinitionModel._FIELDS.label] = "dummy";
		widget[CMDBuild.model.CMWidgetDefinitionModel._FIELDS.active] = true;

		return widget;
	}

	function getADummyWidgetModel() {
		var g = getADummyWidgetConfiguration();
		return new CMDBuild.model.CMWidgetDefinitionModel(g);
	}
})();