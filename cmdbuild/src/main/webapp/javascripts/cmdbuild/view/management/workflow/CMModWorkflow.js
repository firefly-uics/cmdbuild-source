(function() {
	var tr = CMDBuild.Translation.management.modworkflow;

	Ext.define("CMDBuild.view.management.workflow.CMModProcess", {
		extend: "CMDBuild.view.management.classes.CMModCard",
		cmName: "process",

		initComponent: function() {
			this.callParent(arguments);
			_CMUtils.forwardMethods(this, this.cardTabPanel, [
				"getActivityPanel",
				"getRelationsPanel",
				"getHistoryPanel",
				"getAttachmentsPanel",
				"getNotesPanel",
				"buildWidgets",
				"updateDocPanel",
				"getWFWidgets",
				"showActivityPanel",
				"reset"
			]);
		},

		buildComponents: function() {
			var gridratio = CMDBuild.Config.cmdbuild.grid_card_ratio || 50;

			this.cardGrid = new CMDBuild.view.management.workflow.CMActivityGrid({
				hideMode: "offsets",
				filterCategory: this.cmName,
				border: false,
				columns: [],
				forceSelectionOfFirst: true
			});

			this.cardTabPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel({
				region: "south",
				hideMode: "offsets",
				split: true,
				border: false,
				height: gridratio + "%"
			});

			var widgetManager = new CMDBuild.view.management.workflow.CMWidgetManager(this.cardTabPanel);
			this.getWidgetManager = function() {
				return widgetManager;
			}
		}
	});
})();