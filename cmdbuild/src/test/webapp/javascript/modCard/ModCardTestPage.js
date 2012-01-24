(function() {

	Ext.define("ModCardTestPage", {
		extend: "Ext.container.Viewport",
		layout: "border",
		initComponent: function() {
			_CMCache.addClasses(CMDBuild.test.data.ClassesDataSet.getClassesForCache());
			this.tree = new CMDBuild.view.common.classes.CMClassAccordion({
				title: "Classes",
				region: "west",
				split: true,
				width: 200
			});
			this.tree.updateStore();

			this.panel = new CMDBuild.view.management.classes.CMModCard({
				region: "center"
			});

			this.controller = new CMDBuild.controller.management.classes.CMModClassController(this.panel);

			this.items = [this.tree, this.panel];
			this.callParent(arguments);

			this.bindTreeEvents();
		},

		bindTreeEvents: function() {
			var sm = this.tree.getSelectionModel();
			this.mon(sm, "selectionchange", function(tree, selections) {
				this.controller.onViewOnFront(selections[0]);
			}, this);
		}
	});

	Ext.onReady(function() {
		var thePage = new ModCardTestPage();
		var server = CMDBuild.test.CMServer.create();

		server.bindUrl("services/json/schema/modclass/getattributelist", function(req) {
			return {success: true, rows:CMDBuild.test.data.AttributesDataSet.getAttributesFor(req.idClass)};
		});

		server.bindUrl("services/json/management/modcard/getcard", function(req) {
			console.log(req);
			return {
				success: true, 
				card: CMDBuild.test.data.CardsDataSet.getCard(req.Id, req.IdClass)
			};
		});

		server.bindUrl("services/json/management/modcard/deletecard", function(req) {
			return {success: true};
		});

		server.bindUrl("services/json/management/modcard/updatecard", function(req) {
			return {success: true};
		});

		server.bindUrl("services/json/management/modcard/getcardlist", function(req) {
			var cards = CMDBuild.test.data.CardsDataSet.getCardsFor(req.IdClass);
			return {
				success: true,
				results: cards.length,
				rows: cards
			};
		});
	});
})();