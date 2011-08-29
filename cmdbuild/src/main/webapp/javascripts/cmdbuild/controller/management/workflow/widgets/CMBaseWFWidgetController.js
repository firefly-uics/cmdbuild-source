(function() {
	Ext.define("CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController", {
		mixins: {
			observable: 'Ext.util.Observable'
		},

		cmName: "base",
		constructor: function(view, ownerController) {
			if (typeof view != "object") {
				throw "The view of a WFWidgetController must be an object"
			}

			this.view = view;
			this.ownerController = ownerController;
			this.widgetConf = this.view.widgetConf;
			this.outputName = this.widgetConf.outputName;
			this.wiewIdenrifier = this.widgetConf.identifier;

			if (this.view.backToActivityButton) {
				this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
			}
		},

		activeView: function() {
			this.view.cmActivate();
			this.beforeActiveView();
		},

		toString: function() {
			return this.cmName + " WFWidget controller";
		},

		isBusy: function() {
			_debug(this + " is busy");
			return false;
		},

		onEditMode: function() {
			_debug(this + " edit mode");
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

		onBackToActivityButtonClick: function() {
			try {
				this.ownerController.showActivityPanel();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		},

		// template for subclasses
		beforeActiveView: Ext.emptyFn,
		destroy: Ext.emptyFn,
		isValid: function() {
			return true;
		}
	});
})();