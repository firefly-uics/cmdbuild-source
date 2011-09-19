(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController", {
		mixins: {
			observable: 'Ext.util.Observable'
		},
		constructor: function(v, ownerCt) {
			this.view = v;
			this.ownerController = ownerCt;

			this.callBacks = {
				'action-attachment-delete': this.onDeleteAttachmentClick,
				'action-attachment-edit': this.onEditAttachmentClick,
				'action-attachment-download': this.onDownloadAttachmentClick
			};

			this.mon(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
			this.view.on('beforeitemclick', cellclickHandler, this);
			this.view.on("itemdblclick", onItemDoubleclick, this);
			this.view.on('activate', this.view.loadCardAttachments, this.view);
		},

		onEntrySelect: function(selection) {
			this.view.disable();
			this.view.clearStore();

			if (theModuleIsDisabled()) {
				return;
			}

			this.updateViewPrivilegesForTypeId(selection.get("id"));
		},

		updateViewPrivilegesForTypeId: function(entryTypeId) {
			var et = _CMCache.getEntryTypeById(entryTypeId),
				writePrivileges;
			if (et) {
				writePrivileges = et.get("priv_write");
			}
			this.view.updateWritePrivileges(writePrivileges);
		},

		onCardSelected: function(card) {
			if (theModuleIsDisabled()) {
				return;
			}

			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (et && et.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable) {
				this.view.disable();
			} else {
				this.view.enable();
				this.currentCardId = card.get("Id");
				this.currentClassId = card.get("IdClass");
				this.currentCardPrivileges = {
					create: card.get("data.priv_create"),
					write: card.get("data.priv_write")
				};

				this.view.setExtraParams({
					IdClass: this.currentClassId,
					Id: this.currentCardId
				});

				this.view.reloadCard();
			}
		},

		onDeleteAttachmentClick: function(record) {
			Ext.Msg.confirm( tr.delete_attachment, tr.delete_attachment_confirm,
				function(btn) {

					if (btn != 'yes') {
						return;
					}

					CMDBuild.LoadMask.get().show();
					CMDBuild.Ajax.request({
						url : 'services/json/management/modcard/deleteattachment',
						params : {
							"IdClass": this.currentClassId,
							"Id": this.currentCardId,
							"Filename": record.get("Filename")
						},
						waitTitle : CMDBuild.Translation.common.wait_title,
						waitMsg : CMDBuild.Translation.common.wait_msg,
						method : 'POST',
						scope : this,
						success : function() {
							// Defer the call because Alfresco is not responsive
							function deferredCall() {
								CMDBuild.LoadMask.get().hide();
								this.view.reloadCard();
							};

							Ext.Function.createDelayed(deferredCall, CMDBuild.Config.dms.delay, this)();
						}
				 	});

		 		}, this);
		},

		onDownloadAttachmentClick: function(record) {
			var params = {
				"IdClass": this.currentClassId,
				"Id": this.currentCardId,
				"Filename": record.get("Filename")
			};

			var url = 'services/json/management/modcard/downloadattachment?' + Ext.urlEncode(params);
			window.open(url, "_blank");
		},

		onEditAttachmentClick: function(record) {
			var editAttachmentWin = new CMDBuild.Management.EditAttachmentWindow({
				classId: this.currentClassId,
				cardId: this.currentCardId,
				category: record.get("Category"),
				filename: record.get("Filename"),
				description: record.get("Description")
			}).show();

			editAttachmentWin.on("saved", this.view.reloadCard, this.view);
		},

		onAddAttachmentButtonClick: function() {
			var addAttachmentWin = new CMDBuild.Management.AddAttachmentWindow({
				classId: this.currentClassId,
				cardId: this.currentCardId
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