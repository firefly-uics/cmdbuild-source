(function() {
	var TITLE_PREFIX = CMDBuild.Translation.management.modcard.title;

	Ext.define("CMDBuild.view.management.classes.CMModCard", {
		extend: "Ext.panel.Panel",
		cmName: "class",

		constructor: function() {
			this.buildComponents();
			this.callParent(arguments);
		},

		initComponent: function() {
			this.centralPanelItems = [this.cardGrid];

			buildMapPanel.call(this);

			this.centralPanel = new Ext.panel.Panel({
				region: "center",
				layout: "card",
				activeItem: 0,
				hideMode: "offsets",
				border: true,
				frame: false,
				cardGrid: this.cardGrid,
				theMap: this.theMap,
				items: this.centralPanelItems,
				title: " ",
				tools:[{
					type:'minimize',
					scope: this,
					handler: onToolClick
				},{
					type:'maximize',
					scope: this,
					handler: onToolClick
				},{
					type: "restore",
					scope: this,
					handler: onToolClick
				}],
				showGrid: function() {
					this.getLayout().setActiveItem(this.cardGrid.id);
					this.cardGrid.setCmVisible(true);
					this.theMap.setCmVisible(false);
				},
				showMap: function() {
					this.getLayout().setActiveItem(this.theMap.id);
					this.theMap.setCmVisible(true);
					this.cardGrid.setCmVisible(false);
				}
			});

			Ext.apply(this, {
				layout: "border",
				border: false,
				items: [this.centralPanel, this.cardTabPanel]
			});

			this.callParent(arguments);
		},

		onEntrySelected: function(entry) {
			var id = entry.get("id");

			this.cardGrid.updateStoreForClassId(id, {
				cb: function cbUpdateStoreForClassId() {
					this.loadPage(1, {
						cb: function cbLoadPage() {
							try {
								this.getSelectionModel().select(0);
							} catch (e) {/* if empty*/}
						}
					});
				}
			});

			this.cardTabPanel.onClassSelected(id, activateFirst = true);

			this.addCardButton.updateForEntry(entry);
			this.mapAddCardButton.updateForEntry(entry);

			this.updateTitleForEntry(entry);

			this.cardGrid.openFilterButton.enable();
			this.cardGrid.clearFilterButton.disable();
			this.cardGrid.gridSearchField.reset();
		},

		updateTitleForEntry: function(entry) {
			var description = entry.get("text") || entry.get("name");
			this.centralPanel.setTitle(TITLE_PREFIX+description);
		},

		reset: function(id) {
			this.cardTabPanel.reset(id);
		},

		openCard: function(p, retryWithoutFilter) {
			var entry = _CMCache.getEntryTypeById(p.IdClass);
			this.cardGrid.openCard(p, retryWithoutFilter);
			this.cardTabPanel.onClassSelected(p.IdClass, activateFirst = false);
			this.addCardButton.updateForEntry(entry);
			this.mapAddCardButton.updateForEntry(entry);
			this.updateTitleForEntry(entry);
		},

		// private, overridden in subclasses
		buildComponents: function() {
			var gridratio = CMDBuild.Config.cmdbuild.grid_card_ratio || 50;
			var tbar = [
				this.addCardButton = new CMDBuild.AddCardMenuButton({
					classId: undefined,
					disabled: true
				})
			];

			buildMapButton.call(this, tbar);

			this.cardGrid = new CMDBuild.view.management.classes.CMModCard.Grid({
				hideMode: "offsets",
				filterCategory: this.cmName,
				border: false,
				tbar: tbar,
				columns: [],
				forceSelectionOfFirst: true
			});

			this.cardTabPanel = new CMDBuild.view.management.classes.CMCardTabPanel({
				region: "south",
				hideMode: "offsets",
				split: true,
				height: gridratio + "%"
			});
		}
	});

	Ext.define("CMDBuild.view.management.classes.CMModCard.Grid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmVisible: true,

		setCmVisible: function(visible) {
			this.cmVisible = visible;

			if (this.paramsToLoadWhenVisible) {
				this.updateStoreForClassId(this.paramsToLoadWhenVisible.classId, this.paramsToLoadWhenVisible.o);
				this.paramsToLoadWhenVisible = null;
			}

			this.fireEvent("cmVisible", visible);
		},

		updateStoreForClassId: function(classId, o) {
			if (this.cmVisible) {
				this.callParent(arguments);
				this.paramsToLoadWhenVisible = null;
			} else {
				this.paramsToLoadWhenVisible = {
					classId:classId,
					o: o
				};
			}
		}

	});

	var toolClickCallBack = {
		// in these functions wait for the layout after a collapse/expand
		// operation to have the real size of the elements. To do this use
		// the single listener over the "afterlayout" event

		"minimize": function() {
			function _minimize() {
				this.centralPanel.collapse();
				this.mon(this, "afterlayout", function() {
					this.cardTabPanel.show();
					this.cardTabPanel.setHeight(this.startingModuleHeight);
					this.doLayout();
				}, this, {single: true});
	
				this.doLayout();
			}

			toolClickCallBack["restore"].call(this, _minimize);
		},

		"maximize": function() {
			function _maximize() {
				this.cardTabPanel.hide();
	
				this.mon(this, "afterlayout", function() {
					this.centralPanel.expand();
					this.doLayout();
				}, this, {single: true});
		
				this.doLayout();
			}

			toolClickCallBack["restore"].call(this, _maximize);
		},

		"restore": function(cb) {
			this.centralPanel.expand();
			this.centralPanel.setHeight(this.startingGridHeight);
			
			this.mon(this, "afterlayout", function() {
				this.cardTabPanel.show();
				this.cardTabPanel.setHeight(this.startingCardTabPanelHeight);

				this.doLayout();
			}, this, {single: true});

			if (cb) {
				this.mon(this, "afterlayout", cb, this, {single: true});
			}

			this.doLayout();
		}
	};

	function onToolClick(event, toolEl, panel, tool) {
		var type = tool.type;
		if (typeof this.toolsArePressed == "undefined") {
			this.toolsArePressed = true;
			this.startingCentralPanelHeight = this.centralPanel.getHeight();
			this.startingCardTabPanelHeight = this.cardTabPanel.getHeight();
			this.startingModuleHeight = this.getHeight();
		}

		toolClickCallBack[type].call(this);
	} 

	function buildMapButton(tbar) {
		if (CMDBuild.Config.gis.enabled) {

			this.showMapButton = new Ext.button.Button({
				text: CMDBuild.Translation.management.modcard.tabs.map,
				iconCls: 'map',
				scope: this,
				handler: function() {
					this.centralPanel.showMap();
				}
			});

			tbar.push('->', this.showMapButton);
		}
	}

	function buildMapPanel() {
		if (CMDBuild.Config.gis.enabled) {
			this.showGridButton = new Ext.button.Button({
				text: CMDBuild.Translation.management.modcard.add_relations_window.list_tab,
				iconCls: 'table',
				scope: this,
				handler: function() {
					this.centralPanel.showGrid();
				}
			});

			this.mapAddCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				disabled: true
			});

			this.mapAddCardButton.on("cmClick", function(p) {
				this.addCardButton.fireEvent("cmClick", p);
			}, this);

			this.theMap = new CMDBuild.Management.MapPanel({
				tbar: [this.mapAddCardButton,'->', this.showGridButton],
				frame: false,
				border: false
			});

			this.centralPanelItems.push(this.theMap);

			this.getMapPanel = function() {
				return this.theMap;
			};
		}
	}
})();