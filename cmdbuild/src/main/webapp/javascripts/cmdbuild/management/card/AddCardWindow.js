Ext.define("CMDBuild.Management.AddCardWindow", {
	extend: "CMDBuild.PopupWindow",
	classId: undefined, // setted in instantiation
	card: undefined,
	withButtons: true,
	withToolBar: false,

	initComponent:function() {

		this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel( {
			withButtons: this.withButtons,
			withToolBar: this.withToolBar,
			allowNoteFiled: true
		});

		if (!this.withButtons) {
			this.closeButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.close,
				handler : this.close,
				scope : this
			});
			this.buttons = [this.closeButton];
		}

		Ext.apply(this, {
			title : this.className,
			items : this.cardPanel,
			buttonAlign : "center"
		});

		this.callParent(arguments);

		this.on("show", this.loadCard, this);
		this.cardPanel.on("cmdb-close-window", this.close, this);
	},

	loadCard: function() {
		this.cardPanel.onAddCardButtonClick(this.classId, reloadAttributes = true)
	}
});

Ext.define("CMDBuild.Management.AddCardWindowController", {
	constructor: function(view) {
		this.view = view;

		this.view.cardPanel.saveButton.on("click", this.onSaveButtonClick, this);
		this.view.cardPanel.cancelButton.on("click", this.onCancelButtonClick, this);
	},

	onSaveButtonClick: function() {
		var form = this.view.cardPanel.getForm(),
			params = {
				IdClass: this.view.classId,
				Id: this.card ? this.card.get("Id") : -1
			};

		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				params: params,
				scope: this,
				success : function(form, action) {
					this.view.close();
				}
			});
		}
	},

	onCancelButtonClick: function() {
		this.view.close();
	}

//this.saveButtonHandler = function() {
//	var form = _this.cardForm.form.getForm();
//	if (form.isValid()) {
//		form.submit({
//			method : 'POST',
//			url : 'services/json/management/modcard/updatecard',
//			scope: _this,
//			success : function(form, action) {
//				this.fireEvent('cmdbuild-add-card');
//				this.close();
//			}
//		});
//	}
//},
//
//this.cancelButtonHandler = function() {
//	_this.close();
//};
})