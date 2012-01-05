(function() {
	Ext.define("CMDBuild.controller.management.classes.CMWidgetManager", {
		extend: "CMDBuild.controller.management.common.CMBaseWidgetMananager",

		// override
		buildWidgetController: function buildWidgetController(ui, widgetDef, card) {
			var me = this,
				widgetBuilders = {};

			// openReport
			widgetBuilders[CMDBuild.controller.management.common.widgets.CMOpenReportController.WIDGET_NAME] = openReportControllerBuilder;

			// calendar
			widgetBuilders[CMDBuild.controller.management.common.widgets.CMCalendarController.WIDGET_NAME] = calendarControllerBuilder;

			// ping
			widgetBuilders[CMDBuild.controller.management.common.widgets.CMPingController.WIDGET_NAME] = pingControllerBuilder;

			var builder = widgetBuilders[widgetDef.type];
			if (builder) {
				return builder(ui, superController = me, widgetDef, me.view.getFormForTemplateResolver(), card);
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
		},

		// override
		getWidgetLable: function(w) {
			return w.label;
		},
	});

	function openReportControllerBuilder(ui, superController, widgetDef, clientForm, card) {
		return new CMDBuild.controller.management.common.widgets.CMOpenReportController(
			ui,
			superController,
			widgetDef,
			clientForm,
			new CMDBuild.controller.management.common.widgets.CMOpenReportControllerWidgetReader(),
			card
		);
	}

	function calendarControllerBuilder(ui, superController, widgetDef, clientForm, card) {
		return new CMDBuild.controller.management.common.widgets.CMCalendarController(
			ui,
			superController,
			widgetDef,
			clientForm,
			new CMDBuild.controller.management.common.widgets.CMCalendarControllerWidgetReader(),
			card
		);
	}

	function pingControllerBuilder(ui, superController, widgetDef, clientForm, card) {
		return new CMDBuild.controller.management.common.widgets.CMPingController(
			ui,
			superController,
			widgetDef,
			clientForm,
			card
		);
	}
})();
