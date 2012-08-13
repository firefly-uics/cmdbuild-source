(function() {
	/*
	 * Use it as mixin to have basic functionalities for
	 * CMWidgetController
	 */
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

		getTemplateResolverServerVars: function() {
			var out = {};

			if (this.card) {
				if (Ext.getClassName(this.card) == "CMDBuild.model.CMActivityInstance") {
					// Retrieve the process instance because it stores
					// the data. this.card has only the varibles to show in this step
					// (is the activity instance)
					var pi = _CMWFState.getProcessInstance();
					if (pi) {
						// The processes use a new serialization.
						// Add backward compatibility attributes
						// to the card values
						out = Ext.apply({
							"Id": pi.get("Id"),
							"IdClass": pi.get("IdClass"),
							"IdClass_value": pi.get("IdClass_value")
						}, pi.getValues());
					}
				} else {
					out = this.card.raw || this.card.data;
				}
			}

			_debug("Server vars", out);

			return out;
		},

		beforeActiveView: Ext.emptyFn,
		destroy: Ext.emptyFn,
		onEditMode: Ext.emptyFn
	});
})();