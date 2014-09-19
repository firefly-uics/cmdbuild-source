(function() {

	Ext.define("CMDBuild.view.management.common.widgets.linkCards.LinkCards", {
		extend: "Ext.panel.Panel",

		statics: {
			WIDGET_NAME: ".LinkCards"
		},

		delegate: undefined,

		constructor: function(c) {
			this.widget = c.widget;
			this.widgetReader = CMDBuild.management.model.widget.LinkCardsConfigurationReader;
			this.callParent([this.widget]); // to apply the conf to the panel
		},

		setModel: function(m) {
			this.model = m;
		},

		initComponent: function() {
			var c = this.widget;
			var selModel = selectionModelFromConfiguration(c, this);
			var readOnly = this.widgetReader.readOnly(c);
			var theMapIsToSet = (this.widgetReader.enableMap(c) && CMDBuild.Config.gis.enabled);
			var allowEditCard = false;
			var allowShowCard = false;

			if (this.widgetReader.allowCardEditing(c)) {
				var priv = _CMUtils.getClassPrivilegesByName(this.widgetReader.className(c));
				if (priv && priv.write) {
					allowEditCard = true;
				} else {
					allowShowCard = true;
				}
			}

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
				autoScroll : true,
				filterSubcategory : this.widgetReader.id(c),
				selModel: selModel,
				readOnly: readOnly,
				hideMode: "offsets",
				region: "center",
				border: false,
				cmAllowEditCard: allowEditCard,
				cmAllowShowCard: allowShowCard
			});

			this.items = [this.grid];
			this.layout = "border";

			if (theMapIsToSet) {
				this.buildMap();
			}

			Ext.apply(this, {
				hideMode: "offsets",
				border: false,
				frame: false,
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);

			this.mon(this.grid.getSelectionModel(), "select", function(sm, s) {
				this.fireEvent("select", s);
			}, this);

			this.mon(this.grid.getSelectionModel(), "deselect", function(sm, s) {
				this.fireEvent("deselect", s);
			}, this);

			this.mon(this.grid, "beforeload", onBeforeLoad, this);
			// there is a problem with the loadMask, if remove the delay the
			// selection is done before the unMask, then it is reset
			this.mon(this.grid, "load", Ext.Function.createDelayed(onLoad, 1), this);
		},

		updateGrid: function(classId, cqlParams) {
			this.grid.CQL = cqlParams;
			this.grid.store.proxy.extraParams = this.grid.getStoreExtraParams();
			this.grid.updateStoreForClassId(classId);
		},

		hasMap: function() {
			return this.mapPanel != undefined;
		},

		reset: function() {
			var sm = this.grid.getSelectionModel();
			if (sm && typeof sm.reset == "function") {
				sm.reset();
			}

			this.model.reset();
		},


		buildMap: function() {
			this.mapButton = new Ext.Button({
				text: CMDBuild.Translation.management.modcard.tabs.map,
				iconCls: 'map',
				scope: this,
				handler: function() {
					this.fireEvent("CM_toggle_map");
				}
			});

			this.mapPanel = Ext.create('CMDBuild.view.management.classes.map.CMMapPanel', {
				lon: this.widget.StartMapWithLongitude || this.widget.mapLongitude,
				lat: this.widget.StartMapWithLatitude || this.widget.mapLatitude,
				initialZoomLevel: this.widget.StartMapWithZoom || this.widget.mapZoom,
				frame: false,
				border: false
			});

			this.tbar = ["->", this.mapButton];

			this.items = [this.grid, this.mapPanel];
			this.layout = "card";

			this.showMap = function() {
				this.layout.setActiveItem(this.mapPanel.id);

				this.mapPanel.updateSize();

				this.mapPanel.setCmVisible(true);
				this.grid.setCmVisible(false);

//				this.delegate.syncSelections();
			};

			this.showGrid = function() {
				this.layout.setActiveItem(this.grid.id);

				this.grid.setCmVisible(true);
				this.mapPanel.setCmVisible(false);

//				this.delegate.syncSelections();
			};

			this.getMapPanel = function() {
				return this.mapPanel;
			};
		}
//		,
//
//		syncSelections: function() {
//			if (this.model) {
//				this.grid.syncSelections(this.model.getSelections());
//			}
//		}
	});

	function selectionModelFromConfiguration(conf, me) {
		if (me.widgetReader.readOnly(conf)) {
			return new Ext.selection.RowModel();
		}

		if (me.widgetReader.singleSelect(conf)) {
			return new CMDBuild.selection.CMMultiPageSelectionModel({
				mode: "SINGLE",
				idProperty: "Id" // required to identify the records for the data and not the id of ext
			});
		}

		return new CMDBuild.selection.CMMultiPageSelectionModel({
			mode: "MULTI",
			avoidCheckerHeader: true,
			idProperty: "Id" // required to identify the records for the data and not the id of ext
		});
	}

	function onBeforeLoad() {
		this.model.freeze();
	}

	function onLoad() {
		this.model.defreeze();
//		this.syncSelections();
	}

})();