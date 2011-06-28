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
				title: tr.tabs.history
			});

			this.cardNotesPanel = new CMDBuild.view.management.classes.CMCardNotesPanel({
				title: tr.tabs.notes
			});

			this.mdPanel = new CMDBuild.Management.CardMasterDetailTab({
				title: tr.tabs.detail
			});

			this.attachmentPanel = new CMDBuild.view.management.classes.attacchments.CMCardAttachmentsPanel({
				title: tr.tabs.attachments
			});

			this.callParent(arguments);
		},
		
		initComponent: function() {
			this.frame = false;
			this.items = [
				this.cardPanel,
				this.mdPanel,
				this.cardNotesPanel,
				this.cardHistoryPanel,
				this.attachmentPanel
			];

			this.callParent(arguments);
		},
		
		onClassSelected: function(id) {
			this.items.each(function(item) {
				if (item.onClassSelected) {
					item.onClassSelected(id);
				}
			});
		},
		
		onCardSelected: function(card, reloadFields) {
			this.items.each(function(item) {
				if (item.onCardSelected) {
					item.onCardSelected(card, reloadFields);
				}
			});
		},
		
		onAddCardButtonClick: function(idClass, reloadFields) {
			this.items.each(function(item) {
				if (item.onAddCardButtonClick) {
					item.onAddCardButtonClick(idClass, reloadFields);
				}
			});
		}
	});
	
})();