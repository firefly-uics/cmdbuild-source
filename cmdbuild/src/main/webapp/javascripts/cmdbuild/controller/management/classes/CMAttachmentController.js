(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		constructor: function() {
			this.callParent(arguments);

			this.callBacks = {
				'action-attachment-delete': this.onDeleteAttachmentClick,
				'action-attachment-edit': this.onEditAttachmentClick,
				'action-attachment-download': this.onDownloadAttachmentClick
			};

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

			if (theModuleIsDisabled() || !card) {
				return;
			}

			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (et && et.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable) {
				this.view.disable();
			} else {
				this.updateViewPrivilegesForEntryType(et);

				this.view.setExtraParams({
					IdClass: this.card.get("IdClass"),
					Id: this.card.get("Id")
				});

				this.view.reloadCard();
				this.view.enable();
			}
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
			Ext.Msg.confirm( tr.delete_attachment, tr.delete_attachment_confirm,
				function(btn) {

					if (btn != 'yes') {
						return;
					}

					var me = this;
					CMDBuild.LoadMask.get().show();
					CMDBuild.Ajax.request({
						url : 'services/json/management/modcard/deleteattachment',
						params : {
							"IdClass": me.card.get("IdClass"),
							"Id": me.card.get("Id"),
							"Filename": record.get("Filename")
						},
						method : 'POST',
						success : function() {
							// Defer the call because Alfresco is not responsive
							function deferredCall() {
								CMDBuild.LoadMask.get().hide();
								me.view.reloadCard();
							};

							Ext.Function.createDelayed(deferredCall, CMDBuild.Config.dms.delay, me)();
						}
				 	});

		 		}, this);
		},

		onDownloadAttachmentClick: function(record) {
			var params = {
				"IdClass": this.card.get("IdClass"),
				"Id": this.card.get("Id"),
				"Filename": record.get("Filename")
			};

			var url = 'services/json/management/modcard/downloadattachment?' + Ext.urlEncode(params);
			window.open(url, "_blank");
		},

		onEditAttachmentClick: function(record) {
			var editAttachmentWin = new CMDBuild.Management.EditAttachmentWindow({
				classId: this.card.get("IdClass"),
				cardId: this.card.get("Id"),
				category: record.get("Category"),
				filename: record.get("Filename"),
				description: record.get("Description")
			}).show();

			editAttachmentWin.on("saved", this.view.reloadCard, this.view);
		},

		onAddAttachmentButtonClick: function() {
			var addAttachmentWin = new CMDBuild.Management.AddAttachmentWindow({
				classId: this.card.get("IdClass"),
				cardId: this.card.get("Id")
			}).show();

			this.view.mon(addAttachmentWin, "saved", this.view.reloadCard, this.view);
		}
	});

	function theModuleIsDisabled() {
		return CMDBuild.Config.dms.enabled == "false";
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) { 
		var className = event.target.className;

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

	function onItemDoubleclick(grid, model, html, index, e, options) {
		this.onDownloadAttachmentClick(model);
	}
})();