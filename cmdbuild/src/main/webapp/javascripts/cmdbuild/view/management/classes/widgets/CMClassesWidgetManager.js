(function() {
	Ext.define("CMDBuild.view.management.classes.CMWidgetManager", {
		extend: "CMDBuild.view.management.common.CMBaseWidgetManager",

		// override
		_buildWidget: function buildWidget(widget, card) {
			var me = this,
				allowedTypes = {};

			// openReport
			allowedTypes[CMDBuild.view.management.common.widgets.CMOpenReport.WIDGET_NAME] = CMDBuild.view.management.common.widgets.CMOpenReport;

			var widgetClass = allowedTypes[widget.type];
			if (widgetClass && typeof widgetClass == "function") {
				var ui = new widgetClass();
				me.widgetsContainer.addWidgt(ui);
				return ui;
			} else {
				return null;
			}
		}
	});
})();