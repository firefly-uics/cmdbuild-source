(function() {
	Ext.define("CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController", {
		cmName: "base",
		constructor: function(view) {
			if (typeof view != "object") {
				throw "The view of a WFWidgetController must be an object"
			}

			this.view = view;
			this.widgetConf = this.view.widgetConf;
			this.outputName = this.widgetConf.outputName;
			this.singleSelect = this.widgetConf.SingleSelect;
			this.wiewIdenrifier = this.widgetConf.identifier;

		},

		activeView: function() {
			this.view.cmActivate();
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
		}

	});
})();