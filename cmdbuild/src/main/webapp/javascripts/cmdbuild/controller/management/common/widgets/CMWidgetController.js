(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMWidgetController", {

		statics: {
			WIDGET_NAME: ""
		},

		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			if (typeof view != "object") {
				throw "The view of a WidgetController must be an object";
			}

			if (typeof widgetConf != "object") {
				throw "The widget configuration is mandatory";
			}

			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.view = view;
			this.ownerController = ownerController;
			this.widgetConf = widgetConf;
			this.clientForm = clientForm;
			this.card = card;

			this.outputName = this.widgetConf.outputName;
		},

		toString: function() {
			return Ext.getClassName(this);
		},

		isBusy: function() {
			return false;
		},

		getData: function() {
			return null;
		},

		getVariable: function(variableName) {
			try {
				return this.templateResolver.getVariable(variableName);
			} catch (e) {
				_debug("There is no template resolver");
				return undefined;
			}
		},

		getWidgetId: function() {
			return this.widgetConf.id;
		},

		getLabel: function() {
			return this.widgetConf.label;
		},

		isValid: function() {
			return true;
		},

		beforeActiveView: Ext.emptyFn,
		destroy: Ext.emptyFn,
		onEditMode: Ext.emptyFn
	});
})();