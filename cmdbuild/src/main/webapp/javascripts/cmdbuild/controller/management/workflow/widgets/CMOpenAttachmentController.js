(function() {

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMAttachmentController", {
		extend: "CMDBuild.controller.management.classes.attacchments.CMCardAttacchmentsController",

		constructor: function(view, ownerController) {
			this.callParent(arguments);

			this.widgetConf = this.view.widgetConf;
			this.outputName = this.widgetConf.outputName;
			this.singleSelect = this.widgetConf.SingleSelect;
			this.wiewIdenrifier = this.widgetConf.identifier;

			this.activity = this.ownerController.currentActivity;
			if (isANewActivity(this.activity)) {
//				this.view.addAttachmentButton.disable();
			} else {
//				this.view.addAttachmentButton.enable();
				this.currentClassId = this.activity.get("IdClass");
				this.currentCardId = this.activity.get("Id");
			}
		},

		destroy: function() {
			this.mun(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
		},

		beforeActiveView: function() {
			this.view.loadCardAttachments();
		},

		// override
		onAddAttachmentButtonClick: function() {
			if (isANewActivity(this.activity)) {
				new CMDBuild.Msg.error("@@ Errore", "@@ Non puoi salvare le allegati " +
						"senza aver salvato prima l'attività ", popup = false);
			} else {
				this.callParent(arguments);
			}
		},





		// TODO copied from CMBaseWFWidgetController, inherit them
		onEditMode: function() {
			_debug(this + " edit mode");
		},

		getData: function() {
			return null;
		},

		getVariable: function(variableName) {
			return undefined
		},
		activeView: function() {
			this.beforeActiveView();
			this.view.cmActivate();
		},
		isValid: function() {
			return true;
		}
	});
	
	function isANewActivity(a) {
		return typeof a.get != "function";
	}
})();