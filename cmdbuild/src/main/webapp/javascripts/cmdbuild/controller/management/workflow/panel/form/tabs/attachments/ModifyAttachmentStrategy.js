(function () {

	/**
	 * @link CMDBuild.controller.management.classes.attachments.ModifyAttachmentStrategy
	 * @link CMDBuild.controller.management.classes.attachments.ConfirmAttachmentStrategy
	 *
	 * @legacy
	 */
	Ext.define("CMDBuild.controller.management.workflow.panel.form.tabs.attachments.ModifyAttachmentStrategy", {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment'
		],

		ownerController: undefined,

		constructor: function(ownerController) {
			if (!ownerController) {
				throw "Owner controller is needed";
			}

			this.ownerController = ownerController;
		},

		forgeRequestParams: function(attachmentWindow) {
			var params = {
				Metadata: Ext.encode(attachmentWindow.getMetadataValues())
			};

			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.ownerController.getClassId());
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.ownerController.getCardId();
			params["Category"] = attachmentWindow.attachmentRecord.get("Category");
			params["Filename"] = attachmentWindow.attachmentRecord.get("Filename");

			return params;
		},

		doRequest: function(attachmentWindow) {
			var form = attachmentWindow.form.getForm();
			var me = this;

			CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment.confirm({
				form: form,
				params: me.forgeRequestParams(attachmentWindow),
				loadMask: false,
				scope: me,
				success: function(form, action) {
					// Defer the call because Alfresco is not responsive
					Ext.Function.createDelayed(function deferredCall() {
						me.ownerController.view.reloadCard();
						attachmentWindow.unmask();
						attachmentWindow.close();
						CMDBuild.core.LoadMask.hide();
					}, CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ALFRESCO_DELAY), this)();
				},
				failure: function(form, action) {
					attachmentWindow.unmask();
					CMDBuild.core.LoadMask.hide();

					// Workaround to show form submit error
					if (action && action.result && action.result.errors && action.result.errors.length) {
						for (var i=0; i<action.result.errors.length; ++i) {
							this.showError({ status: null }, action.result.errors[i], action);
						}
					} else {
						this.showError({ status: null }, null, action);
					}
				}
			});
		},

		showError: function(response, error, options) { // FIXME: use core interfaces messages classes
			var tr = CMDBuild.Translation.errors || {
				error_message : "Error",
				unknown_error : "Unknown error",
				server_error_code : "Server error: ",
				server_error : "Server error"
			};
			var errorTitle = null;
			var errorBody = {
					text: tr.unknown_error,
					detail: undefined
			};

			if (error) {
				// if present, add the url that generate the error
				var detail = "";
				if (options && options.url) {
					detail = "Call: " + options.url + "\n";
					var line = "";
					for (var i=0; i<detail.length; ++i) {
						line += "-";
					}

					detail += line + "\n";
				}

				// then add to the details the server stacktrace
				errorBody.detail = detail + "Error: " + error.stacktrace;
				var reason = error.reason;
				if (reason) {
					if (reason == 'AUTH_NOT_LOGGED_IN' || reason == 'AUTH_MULTIPLE_GROUPS') {
						Ext.create('CMDBuild.controller.common.sessionExpired.SessionExpired', {
							ajaxParameters: options,
							passwordFieldEnable: reason == 'AUTH_NOT_LOGGED_IN'
						});
					}
					var translatedErrorString = CMDBuild.core.interfaces.messages.Error.formatMessage(reason, error.reasonParameters);
					if (translatedErrorString) {
						errorBody.text = translatedErrorString;
					}
				}
			} else {
				if (!response || response.status == 200 || response.status == 0) {
					errorTitle = tr.error_message;
					errorBody.text = tr.unknown_error;
				} else if (response.status) {
					errorTitle = tr.error_message;
					errorBody.text = tr.server_error_code+response.status;
				}
			}

			CMDBuild.core.Message.error(errorTitle, errorBody, false);
		}
	});

})();
