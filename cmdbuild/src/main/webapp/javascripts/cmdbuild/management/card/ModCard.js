/**
 * This management module handles the card list and the card attributes
 * 
 * @class CMDBuild.Management.ModCard
 * @extends Ext.Component
 */
CMDBuild.Management.ModCard = Ext.extend(CMDBuild.ModPanel, {
	id: 'modcard',
	translation : CMDBuild.Translation.management.modcard,
	hideMode: 'offsets',
    /** needed for managing splitter **/
    split_status: 'restore',

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
		 * */
		this.mapPanel.onFront();
		/*
		 * the onBack method is the same for grid and map:
		 * 		only set the silent mode to true.		
		 * */
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