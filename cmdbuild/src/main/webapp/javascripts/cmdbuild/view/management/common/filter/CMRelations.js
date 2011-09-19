(function() {
	Ext.define("CMDBuild.view.management.common.filter.CMRelations", {
		extend: "Ext.Panel",
		title: CMDBuild.Translation.management.findfilter.relations,
		initComponent: function() {
			this.domainGrid = new CMDBuild.view.management.common.filter.CMDomainGrid({
				idClass: this.idClass,
				region: "north",
				split: true,
				border: false,
				cls: "cmborderbottom"
			});

			this.relations = new CMDBuild.view.management.common.CMTabPanelWithCardGridAndFilter({
				frame: false,
				border: false,
				region: "center",
				selType: "checkboxmodel",
				multiSelect: true,
				filterType: "filterwindow",
				items: [this.grid, this.filter]
			});

			Ext.apply(this, {
				layout: "border",
				items: [this.domainGrid, this.relations]
			});

			this.callParent(arguments);

			this.domainGrid.getSelectionModel().on("selectionchange", function(sm, s) {
				if (s.length > 0) {
					this.currentDomain = s[0]
					this.relations.updateForClassId(this.currentDomain.get("DestClassId"));
					this.relations.setDisabled(this.currentDomain.get("all") || this.currentDomain.get("notInRelation"));

					if (typeof this.currentDomain.cards == "undefined") {
						this.currentDomain.cards = {};
					}
				}
			}, this);

			this.domainGrid.on("cm-select-destination-subclass", function(s) {
				this.relations.updateForClassId(s.get("DestClassId"));
				this.currentDomain.cards = {};
			}, this);

			this.domainGrid.on("cm-check-all", function(recordIndex, checked) {
				var r = this.domainGrid.store.getAt(recordIndex);
				// the fields "all" and "notInRelation" are mutually exclusive
				if (r.get("notInRelation") && checked) {
					r.set("notInRelation", false);
				}
				r.commit();
				if (this.currentDomain && this.currentDomain.get("DestClassId") == r.get("DestClassId")) {
					this.relations.setDisabled(checked);
				}
			}, this);

			this.domainGrid.on("cm-check-notInRelation", function(recordIndex, checked) {
				var r = this.domainGrid.store.getAt(recordIndex);
				// the fields "all" and "notInRelation" are mutually exclusive
				if (r.get("all") && checked) {
					r.set("all", false);
				}
				r.commit();
				if (this.currentDomain && this.currentDomain.get("DestClassId") == r.get("DestClassId")) {
					this.relations.setDisabled(checked);
				}
			}, this);

			this.relations.grid.getSelectionModel().on("select", function(sm, record) {
				this.currentDomain.cards[record.get("IdClass") + "_" + record.get("Id")] = true;
			}, this);

			this.relations.grid.getSelectionModel().on("deselect", function(sm, record) {
				delete this.currentDomain.cards[record.get("IdClass") + "_" + record.get("Id")];
			}, this);
			
			this.relations.grid.pagingBar.on("beforechange", function() {
				this.currentDomain._cards = Ext.apply({}, this.currentDomain.cards);
				this.relations.grid.store.on("load", function() {
					this.currentDomain.cards = Ext.apply({}, this.currentDomain._cards);
				}, this, {single: true});
			}, this);

			this.relations.grid.pagingBar.on('change', function() {
				if (this.currentDomain) {
					var grid = this.relations.grid,
						domain = this.currentDomain;

					this.relations.grid.store.each(function(r) {
						var key = r.get("IdClass") + "_" + r.get("Id");
						if (domain.cards[key]) {
							grid.getSelectionModel().select(r);
						}
					});
				}
			}, this);
		},

		getDataToSend: function() {
			var data = {};
			this.domainGrid.store.each(function(r) {
				var key = r.get("directedId"),
					o = {};

				o["destinationClass"] = r.get("DestClassId");

				if (r.get("all")) {
					o["type"] = "all";
				} else if (r.get("notInRelation")) {
					o["type"] = "notRel";
				} else {
					var cards = [];
					for (var c in r.cards) {
						cards.push(c);
					}

					if (cards.length > 0) {
						o["type"] = "card";
						o["cards"] = cards;
					}
				}

				if (o.type) {
					data[key] = o;
				}
			});

			return data;
		}
	});
})();