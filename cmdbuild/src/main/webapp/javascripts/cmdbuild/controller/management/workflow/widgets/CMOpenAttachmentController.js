(function() {

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMAttachmentController", {
		extend: "CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController",

		constructor: function(view, ownerController, widget, card) {
			this.callParent(arguments);

			this.widgetConf = this.view.widgetConf;
			this.outputName = this.widgetConf.outputName;
			this.singleSelect = this.widgetConf.SingleSelect;
			this.wiewIdenrifier = this.widgetConf.identifier;
			this.card = card;
			this.ownerController = ownerController;

			//this.currentClassId = this.activity.get("IdClass");
			//this.currentCardId = this.activity.get("Id");

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.callParent(arguments);
			this.mun(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
		},

		beforeActiveView: function() {
			this.onCardSelected(this.card);
			this.view.loadCardAttachments();
		},

		// override
		onAddAttachmentButtonClick: function() {
			if (isANewActivity(this.card)) {
				new CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						CMDBuild.Translation.management.modworkflow.extattrs.attachments.must_save_to_add,
						popup = false);

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
		},

		onBackToActivityButtonClick: function() {
			try {
				this.ownerController.showActivityPanel();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		},

		isBusy: function() {
			return false;
		}
	});

	function isANewActivity(a) {
		return typeof a.get != "function" || a.get("Id") == -1;
	}
})();