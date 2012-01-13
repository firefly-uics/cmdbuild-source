(function() {
	var STORE_URL = "testCMMultiPageSelectionModel";

	TestCase("testCMMultiPageSelectionModel", {

		setUp: function() {
			setup.call(this);
		},

		tearDown: function() {
			this.server.restore();
			delete this.store;
			delete this.grid;
		},

		"test store load something": function() {
			this.store.load();
			this.server.respond();

			assertTrue(this.store.getCount() == 3);
			assertTrue(this.store.getTotalCount() == 20);
		},

		"test store load change page": function() {
			this.store.loadPage(1);
			this.server.respond();

			var r1 = this.store.first();

			this.store.loadPage(2);
			this.server.respond();

			var r2 = this.store.first();
			assertTrue(r1.get("name") != r2.get("name"));
		},

		"test selection model start without selections": function() {
			this.store.loadPage(1);
			this.server.respond();

			assertFalse(this.sm.hasSelection());
		},

		"test selection a record": function() {
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.first();
			this.sm.select(r);

			assertTrue(this.sm.hasSelection());
			assertEquals(1, this.sm.getCount());
		},

		"test does not select if model has not idProperty": function() {
			var store = new Ext.data.Store({
				fields: ["name"],
				autoLoad: false,
				pageSize: 3,
				proxy: {
					type: 'ajax',
					url: STORE_URL,
					reader: {
						root: 'rows',
						totalProperty: 'tot'
					}
				}
			}),
			grid = new Ext.grid.Panel({
				renderTo: Ext.getBody(),
				width: 400,
				height: 400,
				store: store,
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel(),
				columns: [
					{ header: 'Name',  dataIndex: 'name' }
				]
			}),
			sm = grid.getSelectionModel();

			store.loadPage(1);
			this.server.respond();
			
			var r = store.first();
			sm.select(r);
			
			assertFalse(sm.hasSelection());
		},

		"test select if model has not idProperty and pass a idProperty to the sm": function() {
			var store = new Ext.data.Store({
				fields: ["name"],
				autoLoad: false,
				pageSize: 3,
				proxy: {
					type: 'ajax',
					url: STORE_URL,
					reader: {
						root: 'rows',
						totalProperty: 'tot'
					}
				}
			}),
			grid = new Ext.grid.Panel({
				renderTo: Ext.getBody(),
				width: 400,
				height: 400,
				store: store,
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
					idProperty: "name"
				}),
				columns: [
					{ header: 'Name',  dataIndex: 'name' }
				]
			}),
			sm = grid.getSelectionModel();

			store.loadPage(1);
			this.server.respond();
			
			var r = store.first();
			sm.select(r);
			
			assertTrue(sm.hasSelection());
		},

		"test selection multiple record": function() {
			this.store.loadPage(1);
			this.server.respond();

			var recs = this.store.getRange();
			this.sm.select(recs);

			assertTrue(this.sm.hasSelection());
			assertEquals(this.store.getCount(), this.sm.getCount());
		},

		"test _addSelection is called on select": function() {
			this.sm._addSelection = sinon.spy();
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.first();
			this.sm.select(r);

			assertTrue(this.sm._addSelection.called);
		},

		"test _removeSelection is called on deselect": function() {
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.first();
			this.sm.select(r);
			this.sm._removeSelection = sinon.spy();

			this.sm.deselect(r);

			assertTrue(this.sm._removeSelection.called);
		},

		"test that _onStoreDidLoad is called": function(queue) {
			var me = this;

			me.sm._onStoreDidLoad = sinon.spy();
			me.store.loadPage(1);
			me.server.respond();

			assertTrue(me.sm._onStoreDidLoad.called);
		},

		"test that _onBeforeStoreLoad the selections are not deleted": function(queue) {
			var pass = false;

			var sm = this.sm;
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.first();
			this.sm.select(r);

			var orig = this.sm._onBeforeStoreLoad;
			this.sm._onBeforeStoreLoad = function() {
				pass = sm.hasSelection();
				orig.apply(sm, arguments);
			};

			this.store.loadPage(2);

			assertTrue(pass);
		},

		"test selection survives on page changed": function() {
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.first();
			this.sm.select(r);

			assertTrue(this.sm.hasSelection());

			this.store.loadPage(2);
			assertTrue(this.sm.hasSelection());
			assertEquals(r.getId(), this.sm.getSelection()[0].getId());
		},

		"test that on load page the view recheck the selected records": function() {
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.last();
			this.sm.select(r);

			this.grid.getView().onRowSelect = sinon.spy();
			this.store.loadPage(1);

			assertTrue(this.grid.getView().onRowSelect.calledWith(2, suppressEvent=true));
		},

		"test that checking the header getSelection does not return a previous selection": function() {
			this.store.loadPage(1);
			this.server.respond();

			var r = this.store.last();
			this.sm.select(r);
			this.sm.onHeaderClick(null, getCheckerHeaderStub(true), getSelectionEventStub());

			assertFalse(this.sm.hasSelection());
		},

		"test deselection after check the header return the deselected": function() {
			this.store.loadPage(1);
			this.server.respond();

			this.sm.onHeaderClick(null, getCheckerHeaderStub(true), getSelectionEventStub());

			var r = this.store.last();
			this.sm.deselect(r);

			assertEquals(r.getId(), this.sm.getSelection()[0].getId());
		},

		"test that the deselection survievs to change page": function() {
			this.store.loadPage(1);
			this.server.respond();
			this.sm.onHeaderClick(null, getCheckerHeaderStub(true), getSelectionEventStub());
			var r = this.store.last();
			this.sm.deselect(r);

			this.store.loadPage(2);
			this.store.loadPage(1);

			assertEquals(r.getId(), this.sm.getSelection()[0].getId());
		},

		"test that on load page the view does not recheck the deselected records": function() {
			this.store.loadPage(1);
			this.server.respond();
			this.sm.onHeaderClick(null, getCheckerHeaderStub(true), getSelectionEventStub());
			var r = this.store.last();
			this.sm.deselect(r);
			this.store.loadPage(2);

			this.grid.getView().onRowSelect = sinon.spy();
			this.store.loadPage(1);

			assertFalse(this.grid.getView().onRowSelect.calledWith(2, suppressEvent=true));
		},

		"test in reverse mode recheck an unchecked row deselect it": function() {
			this.store.loadPage(1);
			this.server.respond();
			this.sm.onHeaderClick(null, getCheckerHeaderStub(true), getSelectionEventStub());
			var r = this.store.last();
			assertEquals(0, this.sm.getCount());
			this.sm.deselect(r);
			assertEquals(1, this.sm.getCount());
			this.sm.select(r);
			assertEquals(0, this.sm.getCount());
		},

		"test mode:SINGLE works on selection in different page": function() {
			setup.call(this, "SINGLE");

			this.store.loadPage(1);
			this.server.respond();
			var a = this.store.first();
			this.sm.select(a);

			this.store.loadPage(2);
			this.server.respond();
			var b = this.store.first();
			this.sm.select(b);

			assertEquals(1, this.sm.getCount());
			assertEquals(b.getId(), this.sm.getSelection()[0].getId());
		}
	});

	function setup(mode) {
		this.server = CMDBuild.test.CMServer.create();

		this.store = new Ext.data.Store({
			model: "Test.User",
			autoLoad: false,
			pageSize: 3,
			proxy: {
				type: 'ajax',
				url: STORE_URL,
				reader: {
					root: 'rows',
					totalProperty: 'tot'
				}
			}
		});

		this.grid = new Ext.grid.Panel({
			renderTo: Ext.getBody(),
			width: 400,
			height: 400,
			store: this.store,
			selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
				mode: mode || "MULTI"
			}),
			columns: [
				{ header: 'Name',  dataIndex: 'name' },
				{ header: 'Email', dataIndex: 'email', flex: 1 },
				{ header: 'Phone', dataIndex: 'phone' }
			]
		});

		this.sm = this.grid.getSelectionModel();
		this.server.bindUrl(STORE_URL, getRows);
	}

	/*
	 * params = {
	 * 	start: integer,
	 * 	limit: integer,
	 * 	page: integer
	 * }
	 * */
	function getRows(params) {
		var rows = [];
		for (var i=params.start, l=params.start + params.limit; i<l; ++i) {
			rows.push({name: "Pippo " + i, email: "pippo@pippo" + i + ".com", phone: i});
		}

		var out = {
			success: true,
			rows: rows,
			tot: 20
		};

		return Ext.JSON.encode(out);
	};

	function getCheckerHeaderStub(checked) {
		return {
			isCheckerHd: true,
			el: {
				hasCls:function() {return !checked;}
			}
		};
	}

	function getSelectionEventStub() {
		return {
			stopEvent: function(){}
		}
	}
})();