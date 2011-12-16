(function() {
	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel", {
		extend: "Ext.panel.Panel",

		constructor: function(config) {

			this.activityTab = new CMDBuild.view.management.workflow.CMActivityPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.card,
				withToolBar: true,
				withButtons: true
			});

			this.cardHistoryPanel = new CMDBuild.view.management.workflow.CMActivityHistoryTab({
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
					this.openNotePanel,
					this.relationsPanel,
					this.cardHistoryPanel,
					this.openAttachmentPanel
				]
			});

			this.docPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel.DocPanel();

			this.callParent(arguments);

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

		updateDocPanel: function(activity) {
			this.docPanel.updateBody(activity);
		},

		showActivityPanel: function() {
			this.acutalPanel.setActiveTab(this.activityTab);
		},

		disableTabs: function() {
			this.openNotePanel.disable();
			this.relationsPanel.disable();
			this.cardHistoryPanel.disable();
			this.openAttachmentPanel.disable();
		},

		showActivityPanelIfNeeded: function() {
			if (this.ignoreTabActivationManagement) {
				this.ignoreTabActivationManagement = false;
				return;
			}

			// var active = this.acutalPanel.getActiveTab().id;
			// if (active == this.widgetsTab.id
				// || active == this.openNotePanel.id
				// || active == this.openAttachmentPanel.id) {
// 
				// this.showActivityPanel();
			// }
		},

		activateFirstTab: function() {
			this.acutalPanel.setActiveTab(this.activityTab);
		},

		activateRelationTab: function() {
			this.acutalPanel.setActiveTab(this.relationsPanel);
		},

		getWidgetButtonsPanel: function() {
			return this.activityTab;
		},

		getActivityPanel: function() {
			return this.activityTab;
		},

		getRelationsPanel: function() {
			return this.relationsPanel;
		},

		getHistoryPanel: function() {
			return this.cardHistoryPanel;
		},

		getAttachmentsPanel: function() {
			return this.openAttachmentPanel;
		},

		getNotesPanel: function() {
			return this.openNotePanel;
		},

		showWidget: function (w) {
			if (w.extattrtype == "openNote") {
				this.openNotePanel.cmActivate();
			} else if (w.extattrtype == "openAttachment"){
				this.openAttachmentPanel.cmActivate();
			}
		}
	});

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