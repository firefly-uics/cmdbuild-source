(function() {

	var TRUE = "1";

	Ext.define("CMDBuild.view.management.workflow.widgets.CMLinkCardsGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",
		cmAllowEditCard: false,
		cmAllowShowCard: false,
		mapPanel: undefined,

		syncSelections: function(s) {
			var sm = this.getSelectionModel();
			sm.clearSelections();

			if (!this.noSelect && s) {
				for (var i = 0, l = s.length; i<l; ++i) {
					var cardId = s[i];
					this.selectByCardId(cardId);
				}
			}
		},

		buildExtraColumns: function() {
			var cols = [];
			var imageColumnConf = {
				header: '&nbsp', 
				width: 30,
				tdCls: "grid-button",
				fixed: true,
				sortable: false,
				align: 'center',
				dataIndex: 'Id',
				menuDisabled: true,
				hideable: false
			};

			if (this.cmAllowEditCard) {
				cols.push(Ext.apply(imageColumnConf, {
					renderer: function() {
						return '<img style="cursor:pointer" class="action-card-edit" src="images/icons/modify.png"/>';
					}
				}));
			}

			if (this.cmAllowShowCard) {
				cols.push(Ext.apply(imageColumnConf, {
					renderer: function() {
						return '<img style="cursor:pointer" class="action-card-show" src="images/icons/zoom.png"/>';
					}
				}));
			}
		
			return cols;
		},

		selectByCardId: function(cardId) {
			var recIndex = this.getStore().find("Id", cardId);
			if (recIndex >= 0) {
				this.getSelectionModel().select(recIndex, true);
			}
		},

		deselectByCardId: function(cardId) {
			var recIndex = this.getStore().find("Id", cardId);
			if (recIndex >= 0) {
				this.getSelectionModel().deselect(recIndex, true);
			}
		},

		shouldSelectFirst: function() {
			return false;
		}

	});

	Ext.define("CMDBuild.view.management.workflow.widgets.CMLinkCards", {
		extend: "Ext.panel.Panel",
		constructor: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;

			this.callParent([this.widgetConf]); // to apply the conf to the panel
		},

		setModel: function(m) {
			this.model = m;
		},

		initComponent: function() {
			var c = this.widgetConf,
				selModel = selectionModelFromConfiguration(c),
				noSelect = isNoSelect(c),
				theMapIsToSet = (c.Map == "enabled" && CMDBuild.Config.gis.enabled),
				allowEditCard = false,
				allowShowCard = false;

			if (c.AllowCardEditing == TRUE) {
				var priv = _CMUtils.getClassPrivileges(c.ClassId);
				if (priv && priv.write) {
					allowEditCard = true;
				} else {
					allowShowCard = true;
				}
			}

			this.grid = new CMDBuild.view.management.workflow.widgets.CMLinkCardsGrid({
				autoScroll : true,
				filterSubcategory : c.identifier,
				selModel: selModel,
				noSelect: noSelect,
				hideMode: "offsets",
				region: "center",
				border: false,
				cmAllowEditCard: allowEditCard,
				cmAllowShowCard: allowShowCard
			});

			this.items = [this.grid];

			if (theMapIsToSet) {
				buildMapStuff.call(this, c);
			} else {
				this.items = [this.grid];
				this.layout = "border";
			}

			Ext.apply(this, {
				hideMode: "offsets",
				border: false,
				frame: false,
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);

			this.mon(this.grid.getSelectionModel(), "select", function(sm, s) {
				this.fireEvent("select", s.get("Id"));
			}, this);

			this.mon(this.grid.getSelectionModel(), "deselect", function(sm, s) {
				this.fireEvent("deselect", s.get("Id"));
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

		getTemplateResolver: function() {
			return this.templateResolver || new CMDBuild.Management.TemplateResolver({
				clientForm: this.clientForm,
				xaVars: this.widgetConf,
				serverVars: this.activity
			});
		},

		syncSelections: function() {
			if (this.model) {
				this.grid.syncSelections(this.model.getSelections());
			}
		},

		hasMap: function() {
			return this.mapPanel != undefined;
		},

		reset: function() {
			var sm = this.grid.getSelectionModel();
			if (typeof sm.reset == "function") {
				sm.reset();
			}

			this.model.reset();
		}
	});

	function selectionModelFromConfiguration(conf) {
		if (isNoSelect(conf)) {
			return new Ext.selection.RowModel();
		}
		if (conf.SingleSelect) {
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

	function isNoSelect(c) {
		return !!c.NoSelect;
	}

	function buildMapStuff(c) {
		this.mapButton = new Ext.Button({
			text: CMDBuild.Translation.management.modcard.tabs.map,
			iconCls: 'map',
			scope: this,
			handler: function() {
				this.fireEvent("CM_toggle_map");
			}
		});

		this.mapPanel = new CMDBuild.Management.MapPanel({
			hideMode: "offsets",
			lon: c.StartMapWithLongitude,
			lat: c.StartMapWithLatitude,
			initialZoomLevel: c.StartMapWithZoom,
			frame: false,
			border: false
		});

		this.tbar = ["->", this.mapButton];

		this.items = [this.grid, this.mapPanel];
		this.layout = "card";

		this.showMap = function() {
			this.layout.setActiveItem(this.mapPanel.id);
		};

		this.showGrid = function() {
			this.layout.setActiveItem(this.grid.id);
		};
	}

	function onBeforeLoad() {
		this.model.freeze();
	}

	function onLoad() {
		this.model.defreeze();
		this.syncSelections();
	}

})();