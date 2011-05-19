(function() {
	
CMDBuild.Management.ActivityTabPanel = Ext.extend(Ext.Panel, {
	constructor: function(config) {		
		this.docPanel = new Ext.Panel({
			autoScroll: true,
			width: (function() {
			    var w = Ext.getBody().getViewSize().width;
			    return w / (4.5);
			})(),
			hideMode: "offsets",
			region: "east",
			id: "activitydocpanel",
			frame: true,
			collapsible: true,
			collapsed: true,
			split: true,
			margins: "0 5 5 0",
			cmargins: "0 5 5 5",
			title: CMDBuild.Translation.management.modworkflow.activitydocumentation,
			html: "",
		    updateBody: function(text) {
		        if (this.body) {
		            this.body.update(text);
		        }
		    }
		});
	
		this.activityTab = new CMDBuild.Management.ActivityTab({
			title: CMDBuild.Translation.management.modworkflow.tabs.card,
			id: "activity_tab",
			layout: "fit",
			cmdbName: CMDBuild.Constants.tabNames.card,
			wfmodule: config.wfmodule
		});
	
		this.optionsTab = new CMDBuild.Management.ActivityOptionTab({
		   title: CMDBuild.Translation.management.modworkflow.tabs.options,
		   id: "activityopts_tab",
		   layout: "fit",
		   disabled: true,
		   cmdbName: CMDBuild.Constants.tabNames.options,
		   activityTab: this.activityTab
		});
		
		
	    this.acutalPanel = new CMDBuild.TabPanel({
	       region: "center",
	       activeTab: 0,
	       border: false,
	       split: true,
	       style: {"border-top": "1px #99BBE8 solid", "border-right": "1px #99BBE8 solid", padding:"1px 0 0 0"},
	       items: [
				this.activityTab, 
				{
					title: CMDBuild.Translation.management.modworkflow.tabs.detail,
					xtype: "panel",
					disabled: true,
					cmdbName: CMDBuild.Constants.tabNames.detail
				},
				this.optionsTab,
				{
	                eventtype: "activity",
	                eventmastertype: "processclass",
					title: CMDBuild.Translation.management.modworkflow.tabs.notes,
					id: "activitynotes_tab",
					xtype: "activitynotestab",
					layout: "fit",
					disabled: true,
					cmdbName: CMDBuild.Constants.tabNames.notes,
					wfmodule: config.wfmodule
				},{
	                eventtype: "activity",
	                eventmastertype: "processclass",
					title: CMDBuild.Translation.management.modworkflow.tabs.relations,
					id: "activityrelations_tab",
					xtype: "cardrelationstab",
					layout: "fit",
					readOnly: true,
					cmdbName: CMDBuild.Constants.tabNames.relations
				},{
	                eventtype: "activity",
	                eventmastertype: "processclass",
					title: CMDBuild.Translation.management.modworkflow.tabs.history,
					id: "activityhistory_tab",
					xtype: "cardhistoytab",
					layout: "fit",
					cmdbName: CMDBuild.Constants.tabNames.history
				},{
	                eventtype: "activity",
	                eventmastertype: "processclass",
					title: CMDBuild.Translation.management.modworkflow.tabs.attachments,
					id: "activityattachments_tab",
					xtype: "cardattachmentstab",
					layout: "fit",
					cmdbName: CMDBuild.Constants.tabNames.attachments,
					wfmodule: config.wfmodule
				},{
					eventtype: "activity",
	                eventmastertype: "processclass",
					title: CMDBuild.Translation.management.modworkflow.tabs.sketch,
					id: "activitysketch_tab",
					xtype: "panel",
					layout: "fit",				
					cmdbName: CMDBuild.Constants.tabNames.sketch,
					listeners: {
						show: function(component) {
							component.doLayout();
						}
					}
				}
			]
	    });
	    CMDBuild.Management.ActivityTabPanel.superclass.constructor.apply(this, arguments);
	},
	
    initComponent : function() {
		Ext.apply(this,{
		  layout: 'border',
		  items : [this.acutalPanel,this.docPanel]
		});

		CMDBuild.Management.ActivityTabPanel.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-reload-process', this.resetDocPanel, this);		
	},
	
	initForClass: function(table) {
		if (!table) {
			return;
		}		
		if (table.tabToOpen) {
			this.acutalPanel.activateTabByAttr('cmdbName', table.tabToOpen);
		}
		this.resetDocPanel();
		this.acutalPanel.items.each(function(tab) {
			if (tab.initForClass) {
				tab.initForClass(table);
			}
		});
	},
	
	onEmptyActivityGrid: function() {
		this.acutalPanel.items.each(function(tab) {
    		if (tab.onEmptyActivityGrid) {
    			tab.onEmptyActivityGrid();
    		}
		});
	},
	
	loadActivity: function(activity) {
		listenCMActivityLoaded.call(this, activity);

		this.updateActivityDoc(activity);
		this.acutalPanel.items.each(function(tab) {
    		if (tab.loadActivity) {
    			tab.loadActivity(activity);
    		} else if(tab.loadCard) {
    			tab.loadCard(activity);
    		}
		});
		if (activity.isnew) {
			this.disableAllTabs();
			this.acutalPanel.activate(this.activityTab);
			this.activityTab.enable();
		} else {
			if (this.acutalPanel.getActiveTab() == this.optionsTab) {
				this.acutalPanel.activate(this.activityTab);
			}
		}
	},
	
	onSelectStateProcess: function(params) {
		this.acutalPanel.items.each(function(i) {
			if (i.onSelectStateProcess) {
				i.onSelectStateProcess(params);
			}
		});
	},
	
	//so you can search and set the active tab even if this is not really a TabPanel
	getComponent: function(name) {
		return this.acutalPanel.getComponent(name);
	},

	resetDocPanel : function() {
		this.docPanel.collapse();
		this.docPanel.updateBody('');
	},
	
	updateActivityDoc : function(evtParams) {
		var activityDoc = evtParams.record.data.ActivityDescription;
		if (activityDoc == undefined || activityDoc == '') {
            activityDoc = '';
        }
        var divDoc = '<div style="padding: 5px">' + activityDoc + '</div>';
        this.docPanel.updateBody(divDoc);
	},
	
	disableAllTabs: function() {
		this.acutalPanel.items.each(function(tab) {
			tab.disable();
		});
	},
	
	enableAllTabs: function() {
		this.acutalPanel.items.each(function(tab) {
			tab.enable();
		});
	},
	
	setActiveTab: function(tabId) {
		this.acutalPanel.setActiveTab(tabId);
	},
	
	processStarted: function(params) {
		this.activityTab.processStarted(params);
		updateActivity
	}
});
	function listenCMActivityLoaded(activity) {
		/* 
		 * the first time that an activity is loaded, the optionsTab finishes his loadActivity
		 * before the activityTab. In this case the manageEditability does not work.
		 * Is necessary to wait because some WF Widgets might depend on some fields of
		 * the activityTab. So before starting the edit mode of the activityTab
		 * it is important that the optionsTab has been loaded.
		 */
		var tabToWait = 2;
		function onActivityLoaded() {
			if (--tabToWait == 0) {
				this.activityTab.manageEditability(activity);
			}
		}
		this.activityTab.on("CMActivityLoaded", onActivityLoaded, this, { single: true });
		this.optionsTab.on("CMActivityLoaded", onActivityLoaded, this, { single: true });
	}

	Ext.reg('activitytabpanel', CMDBuild.Management.ActivityTabPanel);
})();