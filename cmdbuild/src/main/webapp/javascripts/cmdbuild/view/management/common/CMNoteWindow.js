(function() {

Ext.define("CMDBuild.view.management.common.CMNoteWindow", {
	extend: "CMDBuild.PopupWindow",
	masterCard: null,

	initComponent: function() {
		this.title = Ext.String.format("{0} - {1}"
				, CMDBuild.Translation.management.modcard.tabs.notes 
				, this.masterCard.get("Description"));

		this.note = new CMDBuild.view.management.classes.CMCardNotesPanel({
			withButtons: false
		});

		var buttons = [];
		if (this.masterCard && this.masterCard.raw && this.masterCard.raw.priv_write) {
			var saveButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.save,
				handler: saveNote,
				scope: this,
				disabled: true
			});

			var cancelButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					this.note.onCardSelected(this.masterCard);
					this.note.disableModify();
				},
				scope: this,
				disabled: true
			});
			buttons = [saveButton, cancelButton];
		}

		buttons.push(new Ext.Button({
			text: CMDBuild.Translation.common.buttons.close,
			name: 'saveButton',
			formBind: true,
			handler: function() {
				this.destroy();
			},
			scope: this
		}));

		Ext.apply(this, {
			items: [this.note],
			buttonAlign: "center",
			buttons: buttons
		});

		this.callParent(arguments);

		this.on("show", function() {
			this.note.onCardSelected(this.masterCard);
		}, this);

		this.note.on('saved', function() {
			this.destroy();
		}, this);

		this.note.actualForm.on("activate", function() {
			saveButton.enable();
			cancelButton.enable();
		});

		this.note.actualForm.on("deactivate", function() {
			saveButton.disable();
			cancelButton.disable();
		});
	}
});

	function saveNote() {
		var form = this.note.actualForm.getForm();
		var params = {
			IdClass: this.masterCard.get("IdClass"),
			Id: this.masterCard.get("Id")
		}
	
		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				waitTitle : CMDBuild.Translation.common.wait_title,
				waitMsg : CMDBuild.Translation.common.wait_msg,
				scope: this,
				params: params,
				success : function() {
					this.destroy();
				}
			});
		}
	}
})();