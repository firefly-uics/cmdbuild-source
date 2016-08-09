(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.widget.OpenNote'
	]);

	Ext.define("CMDBuild.controller.management.common.widgets.CMOpenNoteController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		constructor: function(view, supercontroller, widget, templateResolver, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			try {
				this.view.updateWritePrivileges(this.card.hasWritePrivileges());
			} catch (e) {
				this.view.updateWritePrivileges(false);
			}

			this.view.disableModify();

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.mun(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		onBackToActivityButtonClick: function() {
			try {
				this.view.hideBackButton();
				this.view.disableModify();
				this.ownerController.activateFirstTab();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		},

		// override
		// if set the value to the html field when is disabled
		// it display null, so set again the value to it the first time that is shown
		beforeActiveView: function() {
			if (!this._alreadyOpened) {
				this.view.loadCard(new CMDBuild.DummyModel(_CMWFState.getProcessInstance().getValues()));
				this._alreadyOpened = true;
			}
		}
	});

	function isANewActivity() {
		var ai = _CMWFState.getActivityInstance();

		if (ai) {
			return  ai.isNew();
		} else {
			return false;
		}
	}

})();