(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.require('CMDBuild.core.constants.Global');

	Ext.define("CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		mixins: {
			attachmentWindowDelegate: "CMDBuild.view.management.CMEditAttachmentWindowDelegate"
		},

		constructor: function() {
			this.callParent(arguments);

			this.callBacks = {
				'action-attachment-delete': this.onDeleteAttachmentClick,
				'action-attachment-edit': this.onEditAttachmentClick,
				'action-attachment-download': this.onDownloadAttachmentClick
			};

			this.confirmStrategy = null;
			this.delegate = null;

			this.mon(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
			this.mon(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view, "itemdblclick", onItemDoubleclick, this);
			this.mon(this.view, 'activate', this.view.loadCardAttachments, this.view);
		},

		onEntryTypeSelected: function() {
			this.callParent(arguments);

			this.view.disable();
			this.view.clearStore();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			if (this.theModuleIsDisabled() || !card) {
				return;
			}

			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (this.disableTheTabBeforeCardSelection(et)) {
				this.view.disable();
			} else {
				this.updateView(et);
			}
		},

		getCard: function() {
			return this.card || null;
		},

		getCardId: function() {
			var card = this.getCard();
			if (card) {
				return card.get("Id");
			}
		},

		getClassId: function() {
			if (this.card) {
				return this.card.get("IdClass");
			}
		},

		updateView: function(et) {
			this.updateViewPrivilegesForEntryType(et);
			this.setViewExtraParams();
			this.view.reloadCard();
			this.view.enable();
		},

		setViewExtraParams: function() {
			var params = {};
			params[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
			params[CMDBuild.ServiceProxy.parameter.CARD_ID] = this.getCardId();

			this.view.setExtraParams(params);
		},

		disableTheTabBeforeCardSelection: function(entryType) {
			return (entryType && entryType.get("tableType") == CMDBuild.core.constants.Global.getTableTypeSimpleTable());
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			this.view.disable();
		},

		updateViewPrivilegesForEntryType: function(et) {
			var writePrivileges = null;
			if (et) {
				writePrivileges = et.get("priv_write");
			}

			this.view.updateWritePrivileges(writePrivileges);
		},

		updateViewPrivilegesForTypeId: function(entryTypeId) {
			var et = _CMCache.getEntryTypeById(entryTypeId);
			this.updateViewPrivilegesForEntryType(et);
		},

		onDeleteAttachmentClick: function(record) {
			var me = this;

			Ext.Msg.confirm(tr.delete_attachment, tr.delete_attachment_confirm,
				function(btn) {
					if (btn != 'yes') {
						return;
					}
					doDeleteRequst(me, record);
		 		}, this);
		},

		onDownloadAttachmentClick: function(record) {
			var params = {
				Filename: record.get("Filename")
			};

			params[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
			params[CMDBuild.ServiceProxy.parameter.CARD_ID] = this.getCardId();

			CMDBuild.ServiceProxy.attachment.download(params);
		},

		onAddAttachmentButtonClick: function() {
			var autocompletionRules = findAutocompletionRules(this);
			var serverVars = CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(this.getCard());
			var templateResolverForm = this.superController ? this.superController.getFormForTemplateResolver() : null;

			// without the form, the template resolver is not able to
			// do its work. This happen if open the attachments
			// window from the Detail Tab
			var me = this;
			if (templateResolverForm) {
				var mergedRoules = mergeRulesInASingleMap(autocompletionRules);

				new CMDBuild.Management.TemplateResolver({
					clientForm: templateResolverForm,
					xaVars: mergedRoules,
					serverVars: serverVars
				}).resolveTemplates({
					attributes: Ext.Object.getKeys(mergedRoules),
					callback: function(o) {
						createWindowToAddAttachment(me, groupMergedRules(o));
					}
				});
			} else {
				createWindowToAddAttachment(me, autocompletionRules);
			}
		},

		onEditAttachmentClick: function(record) {
			new CMDBuild.view.management.CMEditAttachmentWindow({
				metadataValues: record.getMetadata(),
				attachmentRecord: record,
				delegate: this
			}).show();

			this.confirmStrategy = new CMDBuild.controller.management.classes
				.attachments.ModifyAttachmentStrategy(this);
		},


		destroy: function() {
			this.mun(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
			this.mun(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mun(this.view, "itemdblclick", onItemDoubleclick, this);
			this.mun(this.view, 'activate', this.view.loadCardAttachments, this.view);
		},

		theModuleIsDisabled: function() {
			return !CMDBuild.Config.dms.enabled;
		},

		// as attachment window delegate

		onConfirmButtonClick: function(attachmentWindow) {
			var form = attachmentWindow.form.getForm();

			if (!form.isValid()) {
				return;
			}

			if (this.confirmStrategy) {
				CMDBuild.core.LoadMask.show();
				attachmentWindow.mask();
				this.confirmStrategy.doRequest(attachmentWindow);
			}
		}
	});

	function createWindowToAddAttachment(me, metadataValues) {
		new CMDBuild.view.management.CMEditAttachmentWindow({
			metadataValues: metadataValues,
			delegate: me
		}).show();

		me.confirmStrategy = new CMDBuild.controller.management.classes
		.attachments.AddAttachmentStrategy(me);
	}

	function findAutocompletionRules(me) {
		var classId = me.getClassId();
		var rules = {};

		if (classId) {
			var entryType = _CMCache.getEntryTypeById(classId);
			if (entryType) {
				rules = entryType.getAttachmentAutocompletion();
			}
		}

		return rules;
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	};

	function onItemDoubleclick(grid, model, html, index, e, options) {
		this.onDownloadAttachmentClick(model);
	};

	function doDeleteRequst(me, record) {
		var params = {
			Filename: record.get("Filename")
		};

		params[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.getClassId());
		params[CMDBuild.ServiceProxy.parameter.CARD_ID] = me.getCardId();

		CMDBuild.core.LoadMask.show();
		CMDBuild.ServiceProxy.attachment.remove({
			params: params,
			success: function() {
				// Defer the call because Alfresco is not responsive
				Ext.Function.createDelayed(function deferredCall() {
					CMDBuild.core.LoadMask.hide();
					me.view.reloadCard();
				}, CMDBuild.Config.dms.delay, me)();
			},

			failure: function() {
				CMDBuild.core.LoadMask.hide();
			}
		});
	}

	Ext.define("CMDBuild.controller.management.classes.attachments.ConfirmAttachmentStrategy", {
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

			params[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.ownerController.getClassId());
			params[CMDBuild.ServiceProxy.parameter.CARD_ID] = this.ownerController.getCardId();

			return params;
		},

		doRequest: function(attachmentWindow) {
			var form = attachmentWindow.form.getForm();
			var me = this;
			form.submit({ // TODO: should uses proxy and needs a refactor to manage errors
				method: 'POST',
				url: me.url,
				scope: me,
				params: me.forgeRequestParams(attachmentWindow),
				success: function(form, action) {
					// Defer the call because Alfresco is not responsive
					Ext.Function.createDelayed(function deferredCall() {
						me.ownerController.view.reloadCard();
						attachmentWindow.unmask();
						attachmentWindow.close();
						CMDBuild.core.LoadMask.hide();
					}, CMDBuild.Config.dms.delay, this)();
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

		showError: function(response, error, options) {
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
						CMDBuild.app.Login.addAjaxOptions(options);
						CMDBuild.app.Login.setAuthFieldsEnabled(reason == 'AUTH_NOT_LOGGED_IN');
						CMDBuild.app.Login.show();
						return;
					}
					var translatedErrorString = CMDBuild.core.Ajax.formatMessage(reason, error.reasonParameters);
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

			CMDBuild.Msg.error(errorTitle, errorBody, false);
		}
	});

	Ext.define("CMDBuild.controller.management.classes.attachments.AddAttachmentStrategy", {
		extend: "CMDBuild.controller.management.classes.attachments.ConfirmAttachmentStrategy",
		url: 'services/json/attachments/uploadattachment'
	});

	Ext.define("CMDBuild.controller.management.classes.attachments.ModifyAttachmentStrategy", {
		extend: "CMDBuild.controller.management.classes.attachments.ConfirmAttachmentStrategy",
		url: 'services/json/attachments/modifyattachment',
		forgeRequestParams: function(attachmentWindow) {
			var out = this.callParent(arguments);
			out["Category"] = attachmentWindow.attachmentRecord.get("Category");
			out["Filename"] = attachmentWindow.attachmentRecord.get("Filename");

			return out;
		}
	});

	/*
	 * The template resolver want the templates
	 * as a map. Our rules are grouped so I need
	 * to merge them to have a single level map
	 *
	 * To avoid name collision I choose to concatenate
	 * the group name and the meta-data name
	 *
	 * The following two routines do this dirty work
	 */
	var SEPARATOR = "_";
	function mergeRulesInASingleMap(rules) {
		rules = rules || {};
		var out = {};

		for (var groupName in rules) {
			var group = rules[groupName];
			for (var key in group) {
				out[groupName + SEPARATOR + key] = group[key];
			}
		}

		return out;
	}

	function groupMergedRules(mergedRules) {
		var out = {};
		for (var key in mergedRules) {
			var group = null;
			var metaName = null;
			try {
				var s = key.split(SEPARATOR);
				group = s[0];
				metaName = s[1];
			} catch (e) {
				// Pray for my soul
			}

			if (group && metaName) {
				out[group] = out[group] || {};
				out[group][metaName] = mergedRules[key];
			}
		}

		return out;
	}

})();