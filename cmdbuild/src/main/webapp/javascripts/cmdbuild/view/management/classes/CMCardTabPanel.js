(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.CMCardTabPanel", {
		extend: "Ext.tab.Panel",
		
		constructor: function() {
			this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel({
				title: tr.tabs.card,
				withToolBar: true,
				withButtons: true
			});

			this.cardHistoryPanel = new CMDBuild.view.management.classes.CMCardHistoryTab({
				title: tr.tabs.history,
				disabled: true
			});

			this.cardNotesPanel = new CMDBuild.view.management.classes.CMCardNotesPanel({
				title: tr.tabs.notes,
				disabled: true
			});

			this.relationsPanel = new CMDBuild.view.management.classes.CMCardRelationsPanel({
				title: tr.tabs.relations,
				disabled: true
			});

			this.mdPanel = new CMDBuild.view.management.classes.masterDetails.CMCardMasterDetail({
				title: tr.tabs.detail,
				disabled: true
			});

			this.attachmentPanel = new CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel({
				title: tr.tabs.attachments,
				disabled: true
			});

			this.callParent(arguments);

			this.cardPanel.displayMode();
		},
		
		initComponent: function() {
			this.frame = false;
			this.items = [
				this.cardPanel,
				this.mdPanel,
				this.cardNotesPanel,
				this.relationsPanel,
				this.cardHistoryPanel,
				this.attachmentPanel
			];

			this.callParent(arguments);
		},

		onClassSelected: function(id, activateFirst) {
			if (activateFirst) {
				this.activeFirstTab();
			}

			this.items.each(function(item) {
				if (item.onClassSelected) {
					item.onClassSelected(id);
				}
			});
		},

		onCardSelected: function(card, reloadFields, loadRemoteData) {
			this.items.each(function(item) {
				if (item.onCardSelected) {
					item.onCardSelected(card, reloadFields, loadRemoteData);
				}
			});
		},

		reset: function(idClass) {
			this.activeFirstTab();
			this.items.each(function(item) {
				if (item.reset) {
					item.reset();
				}
				if (item.onClassSelected) {
					item.onClassSelected(idClass);
				}
			});
		},

		onAddCardButtonClick: function(idClass, reloadFields) {
			this.setActiveTab(this.cardPanel);
			this.items.each(function(item) {
				if (item.onAddCardButtonClick) {
					item.onAddCardButtonClick(idClass, reloadFields);
				}
			});
		},

		activeFirstTab: function() {
			this.setActiveTab(this.cardPanel);
		}
	});
	
})();