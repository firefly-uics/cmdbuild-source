(function() {
	Ext.define("CMDBuild.view.management.common.CMBaseWidgetManager", {
		constructor: function(mainView) {
			this.mainView = mainView;
		},

		buildWidget: function(widget, card) {
			this.mainView.getWidgetButtonsPanel().addWidget(widget);
			return this._buildWidget(widget, card);
		},

		showWidget: function(w) {
			this.widgetsContainer.showWidget(w);
		},

		buildWidgetsContainer: function() {
			return new CMDBuild.view.management.workflow.CMWFWidgetsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.options,
				autoScroll: true
			});
		},

		reset: 	function reset() {
			if (this.widgetsContainer) {
				this.widgetsContainer.destroy();
			}
	
			this.widgetsContainer = this.buildWidgetsContainer();
			this.mainView.getWidgetButtonsPanel().removeAllButtons();
			this.widgetsMap = {};
		},

		// implement in subclasses
		_buildWidget: function(widget, card) {
			throw "Implement in sublcasses";
		}
	});
})();