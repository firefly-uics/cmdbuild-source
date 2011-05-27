(function() {
/**
 * @class CMDBuild.Management.ModWorkflowClass
 * @extends Ext.Component
 */
CMDBuild.Management.ModWorkflow = Ext.extend(CMDBuild.ModPanel, {
	id: 'modworkflow',
	translation : CMDBuild.Translation.management.modworkflow,
	hideMode:  'offsets',
	split_status: 'restore',
	selectedState : 'open.running',
		
	initComponent: function() {
		var params = {
          panel : this.id + '_panel',
          itemid : 0,
          itemtext : ''
        };
        var theMod = this;
        
        var storeOfState = new Ext.data.JsonStore({
    		autoLoad : true,
    		url: 'services/json/schema/modworkflow/statuses',
    		root: "rows",
    		fields : ['code', 'name', 'id']
    	});
        
        storeOfState.on('load', function(store, records, options){
        	for (var i = 0, l = records.length ; i<l ; i++) {        		
        		if (records[i].data.code == "closed.terminated" || records[i].data.code == "closed.aborted") {
        			store.remove(records[i]);
        		} else {
        			localizeStatusName(records[i]);
        		}
        	}
        }, this);
        
        var localizeStatusName = function(record) {
        	var code = record.data.code;
        	storeOfState.remove(record);
        	record.data.name = CMDBuild.Translation.management.modworkflow.statuses[code];
    		storeOfState.add(record);
        };
        
        this.stateList = new  Ext.ux.form.XComboBox({
        	store: storeOfState,
			name : 'stete',
			hiddenName : 'state',
			valueField : 'code',
			displayField : 'name',
			triggerAction: 'all',			
			allowBlank : false,
			editable: false,
			grow: true
        });
        
        storeOfState.on('load', function(store, records, options){
        	this.stateList.setValue('open.running');           	
        }, this);
        
        this.stateList.on('select', function(combo, record, index){
        	this.setSelectedState(record.data.code);
        }, this);
        
		this.startProcessMenu = new CMDBuild.AddCardMenuButton({
			classId: undefined,
			baseText: this.translation.add_card,
			textPrefix: this.translation.add_card,
			cacheTreeName: CMDBuild.Constants.cachedTableType.processclass,
			eventName: "new_activity"
		});
		
		this.grid_card_ratio = (100 - CMDBuild.Config.cmdbuild.grid_card_ratio)+'%';
		
		this.cardListGrid = new CMDBuild.Management.WorkflowCardListGrid({
			wfmodule: theMod,
			id: 'activitylist_grid',           
			region: 'north',
			height: this.grid_card_ratio,
			eventtype: 'activity',
			eventmastertype: 'processclass',
			baseUrl: 'services/json/management/modworkflow/getactivitylist',
			split: true
		});
		
		this.activityTabPanel = new CMDBuild.Management.ActivityTabPanel({
    		border: false,
    		region: 'center',
    		id: 'activity_tab_panel',
    		layoutOnTabChange: true,		
			deferredRender: false,
			wfmodule: theMod
    	});
		
		new CMDBuild.Management.ActivityTabPanelController(this.activityTabPanel);
		
		Ext.apply(this,{
	        id: this.id + '_panel',
	        modtype: 'processclass',
	        basetitle: this.translation.title,
	        title: this.translation.title,
			tools: [{
	            id: 'up',
                scope: this,
                handler: function(event, tool, panel) { manageSplitter.call(this, 'up', panel); }
	        }, {
                id: 'down',
                scope: this,
                handler: function(event, tool, panel) { manageSplitter.call(this, 'down', panel); }
            }, {
                id: 'restore',
                scope: this,
                handler: function(event, tool, panel) { manageSplitter.call(this, 'restore', panel); }
            }],
            tbar: [this.startProcessMenu, '-' ,this.stateList],
	        layout: 'border',
	        items: [this.cardListGrid, this.activityTabPanel]
		});

		this.on('afterlayout', function() {
			if (!this.restoreHeight) {
				this.restoreHeight = {
					north: this.cardListGrid.getBox().height,
					center: this.activityTabPanel.getBox().height
				};
			}
		}, this);
		
		this.subscribe('cmdb-select-processclass', this.selectClass, this);
		this.subscribe('cmdb-cardsloaded-activity', this.enableAddProcessAction, this);		
		
		CMDBuild.Management.ModWorkflow.superclass.initComponent.apply(this, arguments);
	},
	
	newActivity: function(activity) {
		this.stateList.setValue('open.running');
		this.cardListGrid.deselect();
		this.activityTabPanel.loadActivity(activity);
	},
	
	loadActivity: function(activity) {
		var data = activity.record.data;
		var classId = data.IdClass;
		if (this.currentClassId != classId) {
			var table = CMDBuild.Cache.getTableById(classId);
			if (table) {
				updateSketch.call(this, table);
			}
		}
		this.activityTabPanel.loadActivity(activity);
	},
	
	onEmptyActivityGrid: function() {
		this.enableAddProcessAction();
		this.activityTabPanel.onEmptyActivityGrid();
	},

	enableAddProcessAction: function() {
		this.startProcessMenu.disable();
		if (this.stateList.getValue() == "open.running") {
			if (this.superClass) {
				this.startProcessMenu.setDisabled(this.startProcessMenu.isEmpty());
			} else {
				this.startProcessMenu.setDisabled(!this.privileges.create);
			}
		}	
	},
	
	setSelectedState : function(newState,reloadcard) {
		this.selectedState = newState;
		this.stateList.setValue(newState);
		if(!reloadcard || (reloadcard == true)){
			this.cardListGrid.reloadCard();
			this.activityTabPanel.onSelectStateProcess({state: newState});
		}
	},
	
	getSelectedState : function() {
	   return this.selectedState;
	},
	
	getFlowStatusCodeById: function(id) {
		var store = this.stateList.store;
		var index = store.find('id', id);
		var record = store.getAt(index);
		if (record && record.data && record.data.code) {
			return record.data.code;
		} else {
			return 'closed.aborted'; // it is NOT in the combo!
		}
	},
	
	selectClass: function(table) {
		if (!table) {
            return;
		}
		this.startProcessMenu.setClassId(table);
		this.privileges = {
            create: table.priv_create,
            write: table.priv_write
        };
        this.superClass = table.superclass;
        
        if ((this.currentClassId != table.id) || table.cardId) {
            if (!table.cardId) {
            	table.cardId = 0;
            }
            this.currentClassId = table.id;
            updateSketch.call(this, table);
            var callback = initForClass.createDelegate(this, [table], true);
            CMDBuild.Management.FieldManager.loadAttributes(table.id, callback, true);
        };
    }
	
});
	
	function initForClass(attributeList, table) {
	    var eventParams = {
	        classId: table.id,
	        classAttributes: attributeList,
	        className: table.text,
	        cardId: table.cardId,
	        privileges: this.privileges,
	        superClass: table.superclass,
	        tabToOpen: table.tabToOpen
	    };
	    
	    //on select state the status must be open.running
	    this.stateList.setValue("open.running");
	    this.selectedState = "open.running";
	    
	    this.cardListGrid.initForClass(eventParams);
	    this.activityTabPanel.initForClass(eventParams);
	}	

	function updateSketch(table) {
		var sketchUrl = table.sketch_url;
		var sketchPanel = Ext.getCmp("activitysketch_tab");
		sketchPanel.removeAll();
		if (sketchUrl) {
			sketchPanel.add(new Ext.Panel({
				html: "<img src=\"" + sketchUrl + "\" >",
				layout: "fit",
				autoScroll: true,
				border: false
			}));
			sketchPanel.enable();    		
		} else {
			sketchPanel.disable();
		}
		sketchPanel.doLayout();
	}
	
    function manageSplitter(status, panel) {
    	if (this.split_status == 'restore') {
    		this.restoreHeight = {
				north: this.cardListGrid.getBox().height,
				center: this.activityTabPanel.getBox().height
			};
    	}
		switch(status) {
			case 'up':
				onUp.call(this);
				break;
			case 'down':
				onDown.call(this);
				break;
			case 'restore':
				onRestore.call(this);
				break;
		}
		this.split_status = status;
    }

    function onUp() {
    	this.cardListGrid.setHeight(0).hide();
    	this.activityTabPanel.setHeight(this.restoreHeight.north + this.restoreHeight.center).show();
    	this.doLayout();
    }
    
    function onDown() {
    	this.activityTabPanel.setHeight(0).hide();
    	this.cardListGrid.setHeight(this.restoreHeight.north + this.restoreHeight.center).show();
    }
    
    function onRestore() {
    	this.cardListGrid.setHeight(this.restoreHeight.north).show();
    	this.activityTabPanel.setHeight(this.restoreHeight.center).show();
    	this.doLayout();
    }
})();