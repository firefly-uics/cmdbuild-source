(function() {

	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel", {
		extend: "Ext.panel.Panel",

		constructor: function(config) {

			this.activityTab = new CMDBuild.view.management.workflow.CMActivityPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.card,
				withToolBar: true,
				withButtons: true
			});

			this.widgetsTab = new CMDBuild.view.management.workflow.CMWFWidgetsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.options,
				autoScroll: true
			});

			this.cardHistoryPanel = new CMDBuild.view.management.classes.CMCardHistoryTab({
				title: CMDBuild.Translation.management.modworkflow.tabs.history
			});

			this.openNotePanel = new CMDBuild.view.management.workflow.widgets.CMOpenNotes({
				title: CMDBuild.Translation.management.modworkflow.tabs.notes
			});

			this.relationsPanel = new CMDBuild.view.management.classes.CMCardRelationsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.relations
			});

			this.openAttachmentPanel = new CMDBuild.view.management.workflow.widgets.CMOpenAttachment({
				title: CMDBuild.Translation.management.modworkflow.tabs.attachments
			});

			this.acutalPanel = new Ext.tab.Panel({
				region: "center",
				activeTab: 0,
				border: true,
				frame: false,
				split: true,
				items: [
					this.activityTab,
					this.widgetsTab,
					this.openNotePanel,
					this.relationsPanel,
					this.cardHistoryPanel,
					this.openAttachmentPanel
				]
			});

			this.docPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel.DocPanel();

			this.callParent(arguments);

			this.activityTab.on("cmeditmode", function() {
				this.fireEvent("cmeditmode");
			}, this);

			this.activityTab.on("cmdisplaymode", function() {
				this.fireEvent("cmdisplaymode");
			}, this);

			this.disableTabs();
		},

		initComponent : function() {
			Ext.apply(this,{
				frame: false,
				border: false,
				layout: 'border',
				items : [this.acutalPanel, this.docPanel]
			});

			this.callParent(arguments);
		},
		
		/*
		 o = {
				reloadFields: reloadFields,
				editMode: editMode,
				cb: cb,
				scope: this
			}
		 */
		updateForActivity: function(activity, o) {
			var data = activity.raw || activity.data,
				widgets = data.CmdbuildExtendedAttributes || [];

			this.disableTabs();
			this.widgetsTab.removeAll(autoDestroy = true);
			this.widgetsMap = {};

			Ext.Array.forEach(widgets, function(w, i) {
				var ui = buildWidget.call(this, w, activity);
				if (ui) {
					this.widgetsMap[w.identifier] = ui;
				}
			}, this);
			this.widgetsTab.disable();
			this.activityTab.updateForActivity(activity, o);

			if (typeof activity.get == "function") {
				this.cardHistoryPanel.onCardSelected(activity);
			} else {
				// the new activity has no the get function,
				// so not notify to history the selection
				this.cardHistoryPanel.reset();
			}

			this.showActivityPanelIfNeeded();
		},

		updateForClosedActivity: function(activity) {
			this.widgetsTab.removeAll(autoDestroy = true);
			this.widgetsMap = {};
			this.widgetsTab.disable();
			this.activityTab.updateForClosedActivity(activity);
			this.cardHistoryPanel.onCardSelected(activity);

			this.showActivityPanelIfNeeded();
		},

		getWFWidgets: function() {
			return this.widgetsMap;
		},

		showActivityPanel: function() {
			this.acutalPanel.setActiveTab(this.activityTab);
		},

		disableTabs: function() {
			this.widgetsTab.disable();
			this.openNotePanel.disable();
			this.relationsPanel.disable();
			this.cardHistoryPanel.disable();
			this.openAttachmentPanel.disable();
		},

		showActivityPanelIfNeeded: function() {
			var active = this.acutalPanel.getActiveTab().id;
			if (active == this.widgetsTab.id
				|| active == this.openNotePanel.id
				|| active == this.openAttachmentPanel.id) {

				this.showActivityPanel();
			}
		}
	});

	function buildWidget(widget, activity) {
		var me = this,
			conf = {
				widget: widget,
				activity: activity,
				clientForm: me.activityTab.getForm()
			},
			builders = {
				linkCards: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMLinkCards(conf);
					me.widgetsTab.add(w);

					return w;
				},
				createModifyCard: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMCreateModifyCard(conf);
					me.widgetsTab.add(w);

					return w;
				},
				manageRelation: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMManageRelation(conf);
					me.widgetsTab.add(w);

					return w;
				},
				openNote: function() {
					me.openNotePanel.configure(conf);

					return me.openNotePanel;
				},
				openAttachment: function() {
					me.openAttachmentPanel.configure(conf);

					return me.openAttachmentPanel;
				}
			};

		if (builders[widget.extattrtype]) {
			return builders[widget.extattrtype](widget);
		} else {
			return null;
		}
	}

	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel.DocPanel", {
		extend: "Ext.panel.Panel",
		initComponent: function() {
			Ext.apply(this, {
				autoScroll: true,
				width: "30%",
				hideMode: "offsets",
				region: "east",
				frame: true,
				border: false,
				collapsible: true,
				collapsed: true,
				split: true,
				margins: "5 5 5 0",
				title: CMDBuild.Translation.management.modworkflow.activitydocumentation,
				html: ""
			});
			
			this.callParent(arguments);
		},

		updateBody: function(activity) {
			if (this.body) {
				var activityDoc = activity.data.ActivityDescription || "",
				text = '<div style="padding: 5px">' + activityDoc + '</div>';

				this.body.update(text);
			}
		}
		
	});
	
	/*
			this.activityTab = new CMDBuild.Management.ActivityTab({
				title: CMDBuild.Translation.management.modworkflow.tabs.card,
				id: "activity_tab",
				layout: "fit",
				cmdbName: CMDBuild.Constants.tabNames.card,
				wfmodule: config.wfmodule
			});

			this.optionsTab = new CMDBuild.Management.ActivityOptionTab({
				title : CMDBuild.Translation.management.modworkflow.tabs.options,
				id : "activityopts_tab",
				layout : "fit",
				disabled : true,
				cmdbName : CMDBuild.Constants.tabNames.options,
				activityTab : this.activityTab
			});

			this.acutalPanel = new CMDBuild.TabPanel({
				region: "center",
				activeTab: 0,
				border: false,
				split: true,
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

			this.callParent(arguments);
		},
	
		initComponent : function() {
			Ext.apply(this,{
				layout: 'border',
				items : [this.acutalPanel,this.docPanel]
			});

			this.callParent(arguments);
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
		
//		 * the first time that an activity is loaded, the optionsTab finishes his loadActivity
//		 * before the activityTab. In this case the manageEditability does not work.
//		 * Is necessary to wait because some WF Widgets might depend on some fields of
//		 * the activityTab. So before starting the edit mode of the activityTab
//		 * it is important that the optionsTab has been loaded.
		 
		var tabToWait = 2;
		function onActivityLoaded() {
			if (--tabToWait == 0) {
				this.activityTab.manageEditability(activity);
			}
		}
		this.activityTab.on("CMActivityLoaded", onActivityLoaded, this, { single: true });
		this.optionsTab.on("CMActivityLoaded", onActivityLoaded, this, { single: true });
	}
	*/
})();