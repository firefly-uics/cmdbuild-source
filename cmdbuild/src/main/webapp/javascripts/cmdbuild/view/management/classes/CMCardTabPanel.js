(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.CMCardTabPanel", {
		extend: "Ext.tab.Panel",

		frame: false,

		constructor: function() {
			this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel({
				title: tr.tabs.card,
				border: false,
				withToolBar: true,
				withButtons: true
			});

			this.cardNotesPanel = CMDBuild.configuration.userInterface.isDisabledCardTab(CMDBuild.core.constants.Proxy.CLASS_NOTE_TAB) ? null
				: new CMDBuild.view.management.classes.CMCardNotesPanel({
					title: tr.tabs.notes,
					disabled: true
				})
			;

			this.relationsPanel = CMDBuild.configuration.userInterface.isDisabledCardTab(CMDBuild.core.constants.Proxy.CLASS_RELATION_TAB) ? null
				: new CMDBuild.view.management.classes.CMCardRelationsPanel({
					title: tr.tabs.relations,
					border: false,
					disabled: true
				})
			;

			this.mdPanel = CMDBuild.configuration.userInterface.isDisabledCardTab(CMDBuild.core.constants.Proxy.CLASS_DETAIL_TAB) ? null
				: new CMDBuild.view.management.classes.masterDetails.CMCardMasterDetail({
					title: tr.tabs.detail,
					disabled: true
				})
			;

			this.attachmentPanel = CMDBuild.configuration.userInterface.isDisabledCardTab(CMDBuild.core.constants.Proxy.CLASS_ATTACHMENT_TAB) ? null
				: new CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel({
					title: tr.tabs.attachments,
					disabled: true
				})
			;

			this.callParent(arguments);

			this.cardPanel.displayMode();
		},

		/**
		 * @param {Number} idClass
		 */
		reset: function(idClass) {
			this.activateFirstTab();

			this.items.each(function(item) {
				if (item.reset)
					item.reset();

				if (item.onClassSelected)
					item.onClassSelected(idClass);
			});
		},

		activateRelationTab: function() {
			this.setActiveTab(this.relationsPanel);
		},

		getCardPanel: function() {
			return this.cardPanel;
		},

		getMDPanel: function() {
			return this.mdPanel;
		},

		getHistoryPanel: function() {
			return this.cardHistoryPanel;
		},

		getRelationsPanel: function() {
			return this.relationsPanel;
		},

		getNotePanel: function() { // TODO: substitute with getNotesPanel
			return this.cardNotesPanel;
		},

		// CMTabbedWidgetDelegate

		getAttachmentsPanel: function() {
			return this.attachmentPanel;
		},

		getNotesPanel: function() {
			return this.cardNotesPanel;
		},

		getEmailPanel: function() {
			return this.emailPanel;
		},

		showWidget: function(w) {
			return false; // not implemented yet
		},

		activateFirstTab: function() {
			this.setActiveTab(this.cardPanel);
		}
	});

})();