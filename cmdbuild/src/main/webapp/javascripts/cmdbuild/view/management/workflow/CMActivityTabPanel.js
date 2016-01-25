(function() {

	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel", {
		extend: "Ext.panel.Panel",

		frame: false,
		layout: 'border',

		constructor: function(config) {

			var tabs = CMDBuild.model.CMUIConfigurationModel.processTabs;
			var disabledTabs = _CMUIConfiguration.getDisabledProcessTabs();

			this.activityTab = new CMDBuild.view.management.workflow.CMActivityPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.card,
				border: false,
				withToolBar: true,
				withButtons: true
			});

			this.openNotePanel = isEnabled(disabledTabs, tabs.notes) ? new CMDBuild.view.management.common.widgets.CMOpenNotes({
				title: CMDBuild.Translation.management.modworkflow.tabs.notes,
				border: false
			}) : null;

			this.relationsPanel = isEnabled(disabledTabs, tabs.relations) ? new CMDBuild.view.management.classes.CMCardRelationsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.relations,
				border: false,
				cmWithAddButton: false,
				cmWithEditRelationIcons: false
			}) : null;

			this.openAttachmentPanel = isEnabled(disabledTabs, tabs.attachments) ? new CMDBuild.view.management.common.widgets.CMOpenAttachment({
				title: CMDBuild.Translation.management.modworkflow.tabs.attachments,
				border: false
			}) : null;

			this.acutalPanel = new Ext.tab.Panel({
				region: "center",
				cls: "cmborderright",
				activeTab: 0,
				frame: false,
				border: false,
				split: true
			});

			this.docPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel.DocPanel();

			this.callParent(arguments);

			this.disableTabs();
		},

		initComponent : function() {
			Ext.apply(this,{
				items: [this.acutalPanel, this.docPanel]
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
			if (this.openNotePanel != null) {
				this.openNotePanel.disable();
			}

			if (this.relationsPanel != null) {
				this.relationsPanel.disable();
			}

			if (this.openAttachmentPanel != null) {
				this.openAttachmentPanel.disable();
			}
		},

		showActivityPanelIfNeeded: function() {
			if (this.ignoreTabActivationManagement) {
				this.ignoreTabActivationManagement = false;
				return;
			}
		},

		activateRelationTab: function() {
			if (relationsPanel != null) {
				this.acutalPanel.setActiveTab(this.relationsPanel);
			}
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

		getEmailPanel: function() {
			return this.emailPanel;
		},

		// return false if is not able to manage the widget
		showWidget: function (w) {
			var managedClasses = {
				"CMDBuild.view.management.common.widgets.CMOpenAttachment": function(me) {
					if (me.openAttachmentPanel != null) {
						me.openAttachmentPanel.cmActivate();
					}
				},
				"CMDBuild.view.management.common.widgets.CMOpenNotes": function(me) {
					if (me.openNotePanel != null) {
						me.openNotePanel.cmActivate();
					}
				},
				'CMDBuild.view.management.workflow.tabs.Email': function(me) {
					if (!Ext.isEmpty(me.emailPanel))
						me.emailPanel.cmActivate();
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
				border: true,
				collapsible: true,
				collapsed: true,
				animCollapse: false,
				split: true,
				margin: "0 5 5 0",
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

	function isEnabled(disabledTabs, name) {
		return !Ext.Array.contains(disabledTabs, name);
	}

})();