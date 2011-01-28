/**
 * This management module handles the activity list and the activity attributes
 * 
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
		CMDBuild.log.info("init workflow module");
		
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
			eventName: "startprocess"
		});
		
		this.startProcessMenu.on('startprocess', function(params){
			this.stateList.setValue('open.running');
			var eventParams = {
				classId: params.classId				
			};
			this.activityTabPanel.TabPanel.activateTabByAttr('cmdbName', CMDBuild.Constants.tabNames.card);
			this.publish('cmdb-new-process', eventParams, this);
		}, this);
		
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
    		border : false,
			activeTab : 0,
			layoutOnTabChange : true,
			region: 'center',
			deferredRender: false,
			id: 'activity_tab_panel',
			defaults: {
				hideMode: 'offsets'
			},
			tabitems: [
				{
				title: this.translation.tabs.card,
				id: 'activity_tab',
				xtype: 'activitytab',
				layout: 'fit',
				cmdbName: CMDBuild.Constants.tabNames.card,
				wfmodule: theMod
			},{
				title: this.translation.tabs.detail,
				xtype: 'panel',
				disabled: true,
				cmdbName: CMDBuild.Constants.tabNames.detail
			},{
			   title: this.translation.tabs.options,
			   id: 'activityopts_tab',
			   xtype: 'activityoptiontab',
			   layout: 'fit',
			   disabled: true,
			   cmdbName: CMDBuild.Constants.tabNames.options
			},{
                eventtype: 'activity',
                eventmastertype: 'processclass',
				title: this.translation.tabs.notes,
				id: 'activitynotes_tab',
				xtype: 'activitynotestab',
				layout: 'fit',
				disabled: true,
				cmdbName: CMDBuild.Constants.tabNames.notes,
				wfmodule: theMod
			},{
                eventtype: 'activity',
                eventmastertype: 'processclass',
				title: this.translation.tabs.relations,
				id: 'activityrelations_tab',
				xtype: 'cardrelationstab',
				layout: 'fit',
				readOnly: true,
				cmdbName: CMDBuild.Constants.tabNames.relations
			},{
                eventtype: 'activity',
                eventmastertype: 'processclass',
				title: this.translation.tabs.history,
				id: 'activityhistory_tab',
				xtype: 'cardhistoytab',
				layout: 'fit',
				cmdbName: CMDBuild.Constants.tabNames.history
			},{
                eventtype: 'activity',
                eventmastertype: 'processclass',
				title: this.translation.tabs.attachments,
				id: 'activityattachments_tab',
				xtype: 'cardattachmentstab',
				layout: 'fit',
				cmdbName: CMDBuild.Constants.tabNames.attachments,
				wfmodule: theMod
			},{
				eventtype: 'activity',
                eventmastertype: 'processclass',
				title: this.translation.tabs.sketch,
				id: 'activitysketch_tab',
				xtype: 'panel',
				layout: 'fit',				
				cmdbName: CMDBuild.Constants.tabNames.sketch,
				listeners: {
					show: function(component) {
						component.doLayout();
					}
				}
			}]
    	});

		Ext.apply(this,{
	        id: this.id + '_panel',
	        modtype: 'processclass',
	        basetitle: this.translation.title,
	        title: this.translation.title,
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
		this.subscribe('cmdb-empty-activity', this.onEmptyActivityGrid, this);
		this.subscribe('cmdb-load-activity', this.onLoadActivity, this);
		CMDBuild.Management.ModWorkflow.superclass.initComponent.apply(this, arguments);
	},
	
	onLoadActivity: function(p) {
		var classId = p.record.data.IdClass
		if (this.currentClassId != classId) {
			var table = CMDBuild.Cache.getTableById(classId);
			if (table) {
				this.updateSketch(table);
			}
		}
	},
	
	onEmptyActivityGrid: function() {
		this.publish('cmdb-init-processclass');
		this.enableAddProcessAction();
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
			this.publish('cmdb-select-stateprocess', {state: newState});
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
        var classId = table.id;
        var className = table.text;
        var cardId = table.cardId;
        this.superClass = table.superclass;
        this.startProcessMenu.setClassId(table);
        this.privileges = {
            create: table.priv_create,
            write: table.priv_write
        };
        if ((this.currentClassId != table.id) || cardId) {
        	CMDBuild.log.info('select-class, currentClassId != classId e cardId', cardId);
            if (!cardId) {
                cardId = 0;
            }
            this.currentClassId = classId;
            this.updateSketch(table);
            var callback = this.publishInitClass.createDelegate(this, [classId, className, cardId, table.tabToOpen], true);
            CMDBuild.Management.FieldManager.loadAttributes(classId, callback, true);
        };
    },    
    
    //private
    updateSketch: function(table) {
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
    },
    
    publishInitClass: function(attributeList, classId, className, cardId, tabToOpen) {
        var eventParams = {
            classId: classId,
            classAttributes: attributeList,
            className: className,
            cardId: cardId,
            privileges: this.privileges,
            superClass: this.superClass,
            tabToOpen: tabToOpen
        };
        //this.addCardAction.disable();//to not conflict with the selection of first row after load
        //on select state the status must be open.running
        this.stateList.setValue("open.running");
        this.selectedState = "open.running";
        this.publish('cmdb-init-processclass', eventParams);
    },	
  
  	//private
    onUp: function() {
    	this.cardListGrid.setHeight(0).hide();
    	this.activityTabPanel.setHeight(this.restoreHeight.north + this.restoreHeight.center).show();
    	this.doLayout();
    },
    
    //private   
    onDown: function() {
    	this.activityTabPanel.setHeight(0).hide();
    	this.cardListGrid.setHeight(this.restoreHeight.north + this.restoreHeight.center).show();
    },
    
    //private   
    onRestore: function() {
    	this.cardListGrid.setHeight(this.restoreHeight.north).show();
    	this.activityTabPanel.setHeight(this.restoreHeight.center).show();
    	this.doLayout();
    },
    
    //private    
    manageSplitter: function(status, panel) {
    	if (this.split_status == 'restore') {
    		this.restoreHeight = {
				north: this.cardListGrid.getBox().height,
				center: this.activityTabPanel.getBox().height
			}
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
    }
});
