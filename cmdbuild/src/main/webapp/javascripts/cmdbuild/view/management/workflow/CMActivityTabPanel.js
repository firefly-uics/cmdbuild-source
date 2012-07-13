(function() {
	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel", {
		extend: "Ext.panel.Panel",

		mixins: {
			// cmTabbedWidgetDelegate: "CMDBuild.view.management.common.widgets.CMTabbedWidgetDelegate"
		},

		constructor: function(config) {

			this.activityTab = new CMDBuild.view.management.workflow.CMActivityPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.card,
				withToolBar: true,
				withButtons: true
			});

			this.cardHistoryPanel = new CMDBuild.view.management.workflow.CMActivityHistoryTab({
				title: CMDBuild.Translation.management.modworkflow.tabs.history
			});

			this.openNotePanel = new CMDBuild.view.management.common.widgets.CMOpenNotes({
				title: CMDBuild.Translation.management.modworkflow.tabs.notes
			});

			this.relationsPanel = new CMDBuild.view.management.classes.CMCardRelationsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.relations,
				cmWithAddButton: false,
				cmWithEditRelationIcons: false
			});

			this.openAttachmentPanel = new CMDBuild.view.management.common.widgets.CMOpenAttachment({
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

		// CMTabbedWidgetDelegate

		getAttachmentsPanel: function() {
			return this.openAttachmentPanel;
		},

		getNotesPanel: function() {
			return this.openNotePanel;
		},

		// return false if is not able to manage the widget
		showWidget: function (w) {

			var managedClasses = {
				"CMDBuild.view.management.common.widgets.CMOpenAttachment": function(me) {
					me.openAttachmentPanel.cmActivate();
				},

				"CMDBuild.view.management.common.widgets.CMOpenNotes": function(me) {
					me.openNotePanel.cmActivate();
				}
			};

			var fn = managedClasses[Ext.getClassName(w)];

			if (typeof fn == "function") {
				fn(this);
				return true;
			} else {
				return false;
			}
		},

		activateFirstTab: function() {
			this.acutalPanel.setActiveTab(this.activityTab);
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
				animCollapse: false,
				split: true,
//				margins: "5 5 5 0",
				title: CMDBuild.Translation.management.modworkflow.activitydocumentation,
				html: ""
			});

			this.callParent(arguments);
		},

		updateBody: function(instructions) {
			if (this.body) {
				this.body.update(instructions || "");
			}
		}
	});
})();