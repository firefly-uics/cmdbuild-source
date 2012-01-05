(function() {
	Ext.define("CMDBuild.view.management.classes.CMWidgetManager", {
		extend: "CMDBuild.view.management.common.CMBaseWidgetManager",

		// override
		_buildWidget: function buildWidget(widget, card) {
			var me = this,
				allowedTypes = {};

			// openReport
			allowedTypes[CMDBuild.view.management.common.widgets.CMOpenReport.WIDGET_NAME] = CMDBuild.view.management.common.widgets.CMOpenReport;

			// calendar
			allowedTypes[CMDBuild.view.management.common.widgets.CMCalendar.WIDGET_NAME] = CMDBuild.view.management.common.widgets.CMCalendar;

			// ping
			allowedTypes[CMDBuild.view.management.common.widgets.CMPing.WIDGET_NAME] = CMDBuild.view.management.common.widgets.CMPing;

			var widgetClass = allowedTypes[widget.type];
			if (widgetClass && typeof widgetClass == "function") {
				var ui = new widgetClass({
					hideMode: "offsets"
				});
				me.widgetsContainer.addWidgt(ui);
				return ui;
			} else {
				return null;
			}
		},

		// override
		getFormForTemplateResolver: function() {
			return this.mainView.getForm();
		}
	});
})();