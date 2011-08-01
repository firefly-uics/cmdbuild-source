Ext.define("CMDBuild.controller.management.common.CMCardWindowController", {
	constructor: function(view) {
		this.view = view;

		this.view.cardPanel.saveButton.on("click", this.onSaveButtonClick, this);
		this.view.cardPanel.cancelButton.on("click", this.onCancelButtonClick, this);
	},

	onSaveButtonClick: function() {
		var form = this.view.cardPanel.getForm(),
			params = {
				IdClass: this.view.classId,
				Id: this.view.cardId || -1
			};

		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				params: params,
				scope: this,
				success : function(form, action) {
					this.view.destroy();
				}
			});
		}
	},

	onCancelButtonClick: function() {
		this.view.destroy();
	}
});