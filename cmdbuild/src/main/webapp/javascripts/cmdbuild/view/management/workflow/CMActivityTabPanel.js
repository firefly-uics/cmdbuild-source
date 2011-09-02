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
				title: CMDBuild.Translation.management.modworkflow.tabs.relations,
				cmWithAddButton: false,
				cmWithEditRelationIcons: false
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

		onClassSelected: function(id, activateFirst) {
			if (activateFirst) {
				this.showActivityPanel();
			}

			this.acutalPanel.items.each(function(item) {
				if (item.onClassSelected) {
					item.onClassSelected(id);
				}
			});
		},

		reset: function(idClass) {
			this.showActivityPanel();
			this.acutalPanel.items.each(function(item) {
				if (item.reset) {
					item.reset();
				}
				if (item.onClassSelected) {
					item.onClassSelected(idClass);
				}
			});
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

			this.showActivityPanelIfNeeded();

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
			this.docPanel.updateBody(activity);
		},

		updateForClosedActivity: function(activity) {
			this.showActivityPanelIfNeeded();

			this.widgetsTab.removeAll(autoDestroy = true);
			this.widgetsMap = {};
			this.widgetsTab.disable();
			this.activityTab.updateForClosedActivity(activity);
			this.cardHistoryPanel.onCardSelected(activity);

			this.docPanel.updateBody();
		},

		getWFWidgets: function() {
			return this.widgetsMap;
		},

		onAddButtonClick: function() {
			this.showActivityPanel();
			this.disableTabs();
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
				createModifyCard: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMCreateModifyCard(conf);
					me.widgetsTab.add(w);

					return w;
				},
				createReport: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMCreateReport(conf);
					me.widgetsTab.add(w);

					return w;
				},
				linkCards: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMLinkCards(conf);
					me.widgetsTab.add(w);

					return w;
				},
				manageEmail: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMManageEmail(conf);
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
				var activityDoc;
				if (activity) {
					activityDoc = activity.data.ActivityDescription;
				}

				var text = '<div style="padding: 5px">' + (activityDoc || "") + '</div>';
				this.body.update(text);
			}
		}
	});
})();