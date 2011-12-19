(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMCalendarController", {

		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMCalendarDefinitionForm.WIDGET_NAME
		},

		constructor: function() {
			this.callParent(arguments);
		}
	});
})();