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
				},
				showMap: function() {
					this.getLayout().setActiveItem(this.theMap.id);
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

		openCard: function(p) {
			this.cardGrid.openCard(p);
			this.cardTabPanel.onClassSelected(p.IdClass, activateFirst = false);
			this.addCardButton.updateForEntry(_CMCache.getEntryTypeById(p.IdClass));
			this.cardGrid.clearFilterButton.disable();
			this.cardGrid.gridSearchField.reset();
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

			this.cardGrid = new CMDBuild.view.management.common.CMCardGrid({
				hideMode: "offsets",
				filterCategory: this.cmName,
				border: false,
				tbar: tbar,
				columns: []
			});

			this.cardTabPanel = new CMDBuild.view.management.classes.CMCardTabPanel({
				region: "south",
				hideMode: "offsets",
				split: true,
				height: (100 - gridratio) + "%"
			});
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

			this.theMap = new CMDBuild.Management.MapPanel({
				tbar: ['->', this.showGridButton],
				frame: false,
				border: false
			});

			this.centralPanelItems.push(this.theMap);
		}
	}
/*
CMDBuild.Management.ModCard = Ext.extend(CMDBuild.ModPanel, {
	id: 'modcard',
	translation : CMDBuild.Translation.management.modcard,
	hideMode: 'offsets',

	initComponent: function() {	
		var params = {
          panel : this.id + '_panel',
          itemid : 0,
          itemtext : ''
        };
		
		this.addCardAction = new CMDBuild.AddCardMenuButton({
			classId: undefined,
			eventName: "cmdb-new-card"
		});
		
		this.addCardAction.on("cmdb-new-card", function(p){
			this.tabPanel.newCard();
			this.publish('cmdb-new-card', p);
		}, this);
		
		var toolbarItems = [this.addCardAction];
		
		if (CMDBuild.Config.gis.enabled) {
			this.mapButton = new Ext.Button({
				text: this.translation.tabs.map, 
				iconCls: 'map',
				scope: this,
				handler: function() {
					if (this.northPanel.gridIsVisible) {
						this.northPanel.showMap();
						this.mapButton.setIconClass('table');
						this.mapButton.setText(this.translation.add_relations_window.list_tab);
					} else {
						this.northPanel.showGrid();
						this.mapButton.setIconClass('map');
						this.mapButton.setText(this.translation.tabs.map);
					}
				}
			});
			toolbarItems.push('->', this.mapButton);
		}
		
		this.northPanel = new CMDBuild.Management.ModCard.NorthPanel();
		this.tabPanel = new CMDBuild.Management.ModCard.TabbedPanel({
			mapPanelController: this.northPanel.getMapController()
		});
		
		this.northPanel.on("enableModify", this.enableModify, this);
		
		Ext.apply(this,{
	        id: this.id + '_panel',
	        modtype: 'class',
	        basetitle: this.translation.title,
	        title: this.translation.title,
	        border: true,
	        layout: 'border',
			tools: [{
		            id: 'up',
                    scope: this,
                    handler: function(event, tool, panel) { this.manageSplitter('up', panel); }
		        }, {
	                id: 'down',
                    scope: this,
                    handler: function(event, tool, panel) { this.manageSplitter('down', panel); }
	            }, {
	                id: 'restore',
                    scope: this,
                    handler: function(event, tool, panel) { this.manageSplitter('restore', panel); }
	            }    
		    ],
            tbar: toolbarItems,
	        items: [this.northPanel, this.tabPanel]
		});

		
		function enableAddCardAction() {
			if (this.privileges.create) {
				this.addCardAction.enable();
			}
			if (this.superClass) {
				this.addCardAction.disableIfEmpty();
			}
		}
		
		this.subscribe('cmdb-cardsloaded-card', enableAddCardAction, this);
		this.subscribe('cmdb-empty-card', enableAddCardAction, this);
		this.subscribe('cmdb-select-class', this.onSelectClass, this);
		
		this.on('afterlayout', function() {
			//initialize the height of the noth and central panel for the split manager 
			if (!this.restoreHeight) {
				this.restoreHeight = {
					north: this.northPanel.getBox().height,
					center: this.tabPanel.getBox().height
				};
			}
		}, this);
		
		CMDBuild.Management.ModCard.superclass.initComponent.apply(this, arguments);
	},

	onSelectClass: function(table) {
		if (!table) {
			return;
		}
		this.northPanel.lastSelectedTable = table;
		var classId = table.id;
		var className = table.text;
		var cardId = table.cardId;
		this.superClass = table.superclass;
		this.privileges = CMDBuild.Utils.getClassPrivileges(classId);
		this.addCardAction.setClassId(table);
		if (this.currentClassId != table.id || cardId) {
			if (!cardId) {
				cardId = 0;
			}
			this.currentClassId = classId;
			var callback = this.publishInitClass.createDelegate(this, [classId, className, cardId, this.superClass, table.tabToOpen], true);
			CMDBuild.Management.FieldManager.loadAttributes(classId, callback, true);
		}
		this.setTitle(this.basetitle+className);
		this.tabPanel.onSelectClass();
	},

    publishInitClass: function(attributeList, classId, className, cardId, superClass, tabToOpen) {
    	var eventParams = {
			classId: classId,
			classAttributes: attributeList,
			className: className,
			cardId: cardId,
			privileges: this.privileges,
			superClass: superClass,
			tabToOpen: tabToOpen
		};
		
		this.publish('cmdb-init-class', eventParams);
    },
    
    //private
    onUp: function() {
    	this.northPanel.setHeight(0).hide();
    	this.tabPanel.setHeight(this.restoreHeight.north + this.restoreHeight.center).show();
    	this.doLayout();
    },
    
    //private   
    onDown: function() {
    	this.tabPanel.setHeight(0).hide();
    	this.northPanel.setHeight(this.restoreHeight.north + this.restoreHeight.center).show();
    },
    
    //private   
    onRestore: function() {
    	this.northPanel.setHeight(this.restoreHeight.north).show();
    	this.tabPanel.setHeight(this.restoreHeight.center).show();
    	this.doLayout();
    },
    
    //private    
    manageSplitter: function(status, panel) {
    	if (this.split_status == 'restore') {
    		this.restoreHeight = {
				north: this.northPanel.getBox().height,
				center: this.tabPanel.getBox().height
			};
    	}
		switch(status) {
			case 'up':
				this.onUp();
				break;
			case 'down':
				this.onDown();
				break;
			case 'restore':
				this.onRestore();
				break;
		}
		this.split_status = status;
    },
    
    enableModify: function() {
    	this.northPanel.enableModify();
    	this.tabPanel.enableModify();
    }
});

CMDBuild.Management.ModCard.NorthPanel = Ext.extend(Ext.Panel, {
	hideMode: 'offsets',
	region: 'north',
	split: true,
	frame: false,
	border: false,
	layout: 'card',
	activeItem: 0,
	gridIsVisible: true,
	initComponent: function() {
		this.height = (100 - CMDBuild.Config.cmdbuild.grid_card_ratio)+'%';	
		this.grid = new CMDBuild.Management.CardListGrid({	
			layout: 'fit',
			autoScroll: true
		});
		
		this.grid.on('rowdblclick', function() {
			this.fireEvent("enableModify");
			this.enableModify();
		}, this);
		
		this.items = [this.grid];
		if (CMDBuild.Config.gis.enabled) {
			this.mapPanel = new CMDBuild.Management.MapPanel();
			this.items.push(this.mapPanel);
		}
		CMDBuild.Management.ModCard.NorthPanel.superclass.initComponent.apply(this, arguments);
	},
	showMap: function() {
		this.gridIsVisible = false;
		/*
		 * the onFront method is the same for grid and map:
		 * 		if true update the component and check
		 * 		the last class and card loaded in the CMDBuild state.		
		 * *//*
		this.mapPanel.onFront();
		/*
		 * the onBack method is the same for grid and map:
		 * 		only set the silent mode to true.		
		 * */	/*
		this.grid.onBack();
		this.getLayout().setActiveItem(this.mapPanel.id);
	},
	showGrid: function() {
		this.gridIsVisible = true;
		this.grid.onFront();
		this.mapPanel.onBack();
		this.getLayout().setActiveItem(this.grid.id);
	},
	getMapController: function() {
		var c = undefined;
		if (this.mapPanel) {
			c = this.mapPanel.getController();
		}
		return c;
	},
	enableModify: function() {
		var mapController = this.getMapController();
		if (mapController) {
			mapController.onEnableModify();
		}
	}
});

CMDBuild.Management.ModCard.TabbedPanel = Ext.extend(CMDBuild.Management.CardTabPanel, {
	translation: CMDBuild.Translation.management.modcard,
    collapsible: false,
    animCollapse: false,
    activeTab: 0,
    layoutOnTabChange: true,
    region: 'center',
    deferredRender: false,
    defaults: {
        layout: 'fit'
    },
    style: {
        'border-top': '1px ' + CMDBuild.Constants.colors.blue.border + ' solid',
        padding: '1px 0 0 0'
    },
    border: false,
    frame: false,
    initComponent: function() {
    	var controller = new CMDBuild.Management.CardTabController(undefined, subscribeToEvent = true);
    	if (this.mapPanelController) {
    		controller.addCardExtensionProvider(this.mapPanelController);
    	}
    	
    	var cardTabUI = new CMDBuild.Management.CardTabUI({
			title: this.translation.tabs.card,
			id: 'card_tab',
			xtype: 'cardtab',
			cmdbName: CMDBuild.Constants.tabNames.card,
			border: false,
			frame: false,
			controller: controller
		});
    	controller.setView(cardTabUI);
    	
    	this.enableModify = function() {
    		cardTabUI.enableModify();
    	};
    	
    	var _this = this;
		this.items = [
		    cardTabUI,
		{
			title: this.translation.tabs.detail,
			id: 'cardmasterdetail_tab',
			xtype: 'cardmasterdetailtab',
			cmdbName: CMDBuild.Constants.tabNames.detail,
			editable: true,
			border: false,
			frame: false,
			listeners: {
		    	"empty": function() {
		    		_this.setActiveTab(cardTabUI);
		    	}
		    }
		},{
			title: this.translation.tabs.notes,
			id: 'cardnotes_tab',
			xtype: 'cardnotestab',
			cmdbName: CMDBuild.Constants.tabNames.notes,
			border: false,
			frame: false
		},{
			title: this.translation.tabs.relations,
			id: 'cardrelations_tab',
			xtype: 'cardrelationstab',
			cmdbName: CMDBuild.Constants.tabNames.relations,
			border: false,
			frame: false
		},{
			title: this.translation.tabs.history,
			id: 'cardhistory_tab',
			xtype: 'cardhistoytab',
			cmdbName: CMDBuild.Constants.tabNames.history,
			border: false,
			frame: false
		},{
			title: this.translation.tabs.attachments,
			id: 'cardattachments_tab',
			xtype: 'cardattachmentstab',
			cmdbName: CMDBuild.Constants.tabNames.attachments,
			border: false,
			frame: false
		}];
		CMDBuild.Management.ModCard.TabbedPanel.superclass.initComponent.apply(this, arguments);
    },
    
    newCard: function() {
    	this.activateTabByAttr('cmdbName', CMDBuild.Constants.tabNames.card);
    },
    
    onSelectClass: function(params) {
    	// allow the migraton from event bus, to
    	// direct listeners and controllers system
    	this.items.each(function(tab) {
    		if (tab.onSelectClass) {
    			tab.onSelectClass(params);
    		}
    	});
    }
});

*/
})()