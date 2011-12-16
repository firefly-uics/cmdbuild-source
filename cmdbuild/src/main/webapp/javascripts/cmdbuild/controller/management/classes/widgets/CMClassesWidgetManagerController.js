(function() {
	Ext.define("CMDBuild.controller.management.classes.CMWidgetManager", {
		extend: "CMDBuild.controller.management.common.CMBaseWidgetMananager",

		// override
		buildWidgetController: function buildWidgetController(ui, widgetDef) {
			var me = this,
				widgetBuilders = {};

			// openReport
			widgetBuilders[CMDBuild.controller.management.common.widgets.CMOpenReportController.WIDGET_NAME] = openReportControllerBuilder;

			var builder = widgetBuilders[widgetDef.type];
			if (builder) {
				return builder(ui, superController = me, widgetDef);
			} else {
				return null;
			}
		},

		// override
		takeWidgetFromCard: function(card) {
			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (et) {
				return et.getWidgets();
			} else {
				return [];
			}
		},

		// override
		getWidgetId: function(w) {
			return w.id;
		}
	});

	function openReportControllerBuilder(ui, superController, widgetDef) {
		var controller = new CMDBuild.controller.management.common.widgets.CMOpenReportController(ui, superController, widgetDef);
		controller.setWidgetReader(new CMDBuild.controller.management.common.widgets.CMOpenReportControllerWidgetReader());
		return controller;
	}
})();
