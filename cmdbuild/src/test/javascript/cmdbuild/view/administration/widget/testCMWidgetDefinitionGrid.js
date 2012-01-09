(function() {
	TestCase("testCMWidgetDefinitionGrid", {

		setUp: function() {
			this.grid = new CMDBuild.view.administration.widget.CMWidgetDefinitionGrid();
		},

		tearDown: function() {
			delete this.grid;
		},

		"test start with no record": function() {
			assertEquals(0, this.grid.count());
		},

		"test addRecord": function() {
			assertEquals(0, this.grid.count());
			var r = new CMDBuild.model.CMWidgetDefinitionModel();
			this.grid.addRecord(r);
			assertEquals(1, this.grid.count());
		},

		"test addRecord with selectAfter": function() {
			assertEquals(0, this.grid.count());
			var select = sinon.stub(this.grid.getSelectionModel(), "select");
			var r = new CMDBuild.model.CMWidgetDefinitionModel();
			this.grid.addRecord(r, selectAfter = true);
			assertTrue(select.called);
		},

		"test removeRecordWithId do nothing if does not found the id": function() {
			var r = new CMDBuild.model.CMWidgetDefinitionModel({id: "foo"});
			this.grid.addRecord(r);
			assertEquals(1, this.grid.count());
			this.grid.removeRecordWithId("Agamennone");
			assertEquals(1, this.grid.count());
		},

		"test removeRecordWithId remove the record if found the id": function() {
			var id = "foo";
			var r = new CMDBuild.model.CMWidgetDefinitionModel({id: id});
			this.grid.addRecord(r);
			assertEquals(1, this.grid.count());
			this.grid.removeRecordWithId(id);
			assertEquals(0, this.grid.count());
		},

		"test clear selection forward the call to the selModel": function() {
			var mock = sinon.mock(this.grid.getSelectionModel()).expects("deselectAll").once();
			this.grid.clearSelection();
			mock.verify();
		}
	});
})();