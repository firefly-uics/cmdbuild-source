(function() {
	var CLOSED_CODE = "closed.completed",
		TRUE = "true";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityAttachmentsController", {
		extend : "CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController",

		// override
		// we want the attachments in readOnly mode, so set the privilege
		// to can only read. Then if there is the OpenAttachement extend attribute
		// it'll enable the editing

		// new business rule: read a configuration parameter to enable the editing
		// of attachments of closed activities
		updateViewPrivilegesForEntryType: function(et) {
			var priv = false;
			if (CMDBuild.Config.workflow.add_attachment_on_closed_activities == TRUE &&
					this.card &&
					this.card.raw &&
					this.card.raw.FlowStatus_code == CLOSED_CODE) {

				priv = true;
			}

			this.view.updateWritePrivileges(priv);
		},

		// override
		// It is not possible add an attachment at the first step of the process
		onAddAttachmentButtonClick: function() {
			if (isANewActivity(this.card)) {
				new CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						CMDBuild.Translation.management.modworkflow.extattrs.attachments.must_save_to_add,
						popup = false);

			} else {
				this.callParent(arguments);
			}
		},

		// override
		// to avoid the enable of the tab when the user is editing a new
		// activity
		disableTheTabBeforeCardSelection: function(entryType) {
			var superCondition = this.callParent(arguments);
			return superCondition || isANewActivity(this.card);
		},

		// override
		updateView: function() {
			this.callParent(arguments);
			this.view.hideBackButton();
		}
	});

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMAttachmentController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",

		constructor: function(view, ownerController, widget, card) {
			this.callParent(arguments);

			this.card = card;

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.callParent(arguments);
			this.mun(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		// override
		getVariable: function(variableName) {
			return undefined
		},

		activeView: function() {
			this.view.cmActivate();
		},

		onBackToActivityButtonClick: function() {
			try {
				this.view.hideBackButton();
				this.ownerController.showActivityPanel();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		}
	});

	function isANewActivity(a) {
		return typeof a.get != "function" || a.get("Id") == -1;
	}
})();