(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.controller.management.classes.attachments.ConfirmAttachmentStrategy", {
		ownerController: undefined,
		constructor: function(ownerController) {
			if (!ownerController) {
				throw "Owner controller is needed";
			}

			this.ownerController = ownerController;
		},

		forgeRequestParams: function(attachmentWindow) {
			return {
				IdClass: this.ownerController.getClassId(),
				Id: this.ownerController.getCardId(),
				Metadata: Ext.encode(attachmentWindow.getMetadataValues())
			};
		},

		doRequest: function(attachmentWindow) {
			var form = attachmentWindow.form.getForm();
			var me = this;
			form.submit({
				method: 'POST',
				url: me.url,
				scope: me,
				params: me.forgeRequestParams(attachmentWindow),
				success: function() {
					// Defer the call because Alfresco is not responsive
					Ext.Function.createDelayed(function deferredCall() {
						me.ownerController.view.reloadCard();
						attachmentWindow.unmask();
						attachmentWindow.close();
						CMDBuild.LoadMask.get().hide();
					}, CMDBuild.Config.dms.delay, this)();
				},
				failure: function () {
					attachmentWindow.unmask();
					CMDBuild.LoadMask.get().hide();
				}
			});
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
			out["Filename"] = attachmentWindow.attachmentRecord.get("Filename");

			return out;
		}
	});

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

		getCardId: function() {
			if (this.card) {
				return this.card.get("Id");
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
			this.view.setExtraParams({
				IdClass: this.getClassId(),
				Id: this.getCardId()
			});
		},

		disableTheTabBeforeCardSelection: function(entryType) {
			return (entryType && entryType.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable);
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			this.view.disable();
		},

		updateViewPrivilegesForEntryType: function(et) {
			var writePrivileges;
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
			CMDBuild.ServiceProxy.attachment.download({
				IdClass: this.getClassId(),
				Id: this.getCardId(),
				Filename: record.get("Filename")
			});
		},

		onEditAttachmentClick: function(record) {
			var editAttachmentWin = new CMDBuild.view.management.CMEditAttachmentWindow({
				attachmentRecord: record,
				delegate: this
			}).show();

			this.confirmStrategy = new CMDBuild.controller.management.classes
				.attachments.ModifyAttachmentStrategy(this);
		},

		onAddAttachmentButtonClick: function() {
			var addAttachmentWin = new CMDBuild.view.management.CMEditAttachmentWindow({
				delegate: this
			}).show();

			this.confirmStrategy = new CMDBuild.controller.management.classes
				.attachments.AddAttachmentStrategy(this);
		},

		destroy: function() {
			this.mun(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
			this.mun(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mun(this.view, "itemdblclick", onItemDoubleclick, this);
			this.mun(this.view, 'activate', this.view.loadCardAttachments, this.view);
		},

		theModuleIsDisabled: function() {
			return CMDBuild.Config.dms.enabled == "false";
		},

		// as attachment window delegate

		onConfirmButtonClick: function(attachmentWindow) {
			var form = attachmentWindow.form.getForm();

			if (!form.isValid()) {
				return;
			}

			if (this.confirmStrategy) {
				CMDBuild.LoadMask.get().show();
				attachmentWindow.mask();
				this.confirmStrategy.doRequest(attachmentWindow);
			}
		}
	});

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
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.attachment.remove({
			params : {
				IdClass: me.getClassId(),
				Id: me.getCardId(),
				Filename: record.get("Filename")
			},
			success : function() {
				// Defer the call because Alfresco is not responsive
				function deferredCall() {
					CMDBuild.LoadMask.get().hide();
					me.view.reloadCard();
				};

				Ext.Function.createDelayed(deferredCall, CMDBuild.Config.dms.delay, me)();
			},
			failure: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();