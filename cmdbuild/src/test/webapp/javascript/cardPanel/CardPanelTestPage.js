(function() {

	Ext.define("CardPanelTestPage", {
		extend: "Ext.container.Viewport",
		layout: "border",
		initComponent: function() {
			_CMCache.addClasses(CMDBuild.test.data.ClassesDataSet.getClassesForCache());
			this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel({
				region: "center",
				withButtons: true,
				withToolBar: true
			});

			this.logPanel = new LogPanel({
				region: "south",
				split: true,
				height: 200,
				autoScroll: true
			});

			this.commandPanel = new Ext.panel.Panel({
				region: "east",
				width: 200,
				split: true,
				frame: true,
				layout: {
					type:'vbox',
					padding:'5',
					align:'stretch'
				}
			});

			this.cardPanel.displayMode();
			this.items = [this.cardPanel, this.logPanel, this.commandPanel];
			this.callParent(arguments);

			this.cardPanelController = new CMDBuild.controller.management.classes.CMCardPanelController(this.cardPanel);
			this.logPanel.bindObject("CONTROLLER", this.cardPanelController);
			this.addSelectClassCommandButton();
			this.addLoadCardCommandButton();
			this.addNewCardCommandButton();
		},

		addSelectClassCommandButton: function() {
			var me = this;
			this.classes = new CMDBuild.field.CMBaseCombo({
				store: _CMCache.getClassesStore(),
				width: 260,
				valueField : 'id',
				displayField : 'description',
				queryMode: "local"
			});
			var button = new Ext.button.Button({
				text: "onEntryTypeSelected",
				handler: function() {
					me.cardPanelController.onEntryTypeSelected(_CMCache.getClassById(me.classes.getValue()));
				}
			});

			var panel = new Ext.panel.Panel({
				items: [this.classes, button]
			});

			this.commandPanel.add(panel);
		},

		addLoadCardCommandButton: function() {
			var me = this,
				data = {
					Puzzolo: "Puzzolo " + new Date(),
					BellaZio: "Bella Zio " + new Date(),
					Id: 5
				};

			var button = new Ext.button.Button({
				text: "onCardSelected",
				handler: function() {
					data.IdClass = me.classes.getValue();
					me.cardPanelController.onCardSelected(new CMDBuild.DummyModel(data));
				}
			});

			this.commandPanel.add(button);
		},

		addNewCardCommandButton: function() {
			var me = this,
				button = new Ext.button.Button({
					text: "onAddCardButtonClick",
					handler: function() {
						me.cardPanelController.onAddCardButtonClick(me.classes.getValue());
					}
				});

			this.commandPanel.add(button);
		},

		getClass: function() {
			return this.classes.getValue();
		}
	});

	Ext.onReady(function() {
		var thePage = new CardPanelTestPage();
		var server = CMDBuild.test.CMServer.create();

		server.bindUrl("services/json/schema/modclass/getattributelist", function(req) {
			return {success: true, rows:CMDBuild.test.data.AttributesDataSet.getAttributesFor(req.idClass)};
		});

		server.bindUrl("services/json/management/modcard/getcard", function(req) {
			return {success: true, card: {
				Puzzolo: "Remote - Puzzolo " + new Date(),
				BellaZio: "Remove - Bella Zio " + new Date(),
				IdClass: thePage.getClass(),
				Id: 5
			}};
		});

		server.bindUrl("services/json/management/modcard/deletecard", function(req) {
			return {success: true};
		});

		server.bindUrl("services/json/management/modcard/updatecard", function(req) {
			return {success: true};
		});
	});

})();
