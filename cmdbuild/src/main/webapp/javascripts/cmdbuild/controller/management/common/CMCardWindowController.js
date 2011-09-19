Ext.define("CMDBuild.controller.management.common.CMCardWindowController", {
	constructor: function(view) {
		this.view = view;

		if (this.view.withButtons) {
			this.view.cardPanel.saveButton.on("click", this.onSaveButtonClick, this);
			this.view.cardPanel.cancelButton.on("click", this.onCancelButtonClick, this);
		}
	},

	getForm: function() {
		return this.view.cardPanel.getForm()
	},

	onSaveButtonClick: function() {
		var form = this.getForm(),
			params = this.buildSaveParams();

		this.beforeRequest(form);

		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				params: params,
				scope: this,
				success : this.onSaveSuccess
			});
		}
	},

	onCancelButtonClick: function() {
		this.view.destroy();
	},
	
	// private, overridden in subclasses
	buildSaveParams: function() {
		return {
			IdClass: this.view.classId,
			Id: this.view.cardId || -1
		};
	},

	// private, overridden in subclasses
	onSaveSuccess: function(form, action) {
		_CMCache.onClassContentChanged(this.view.classId);
		this.view.destroy();
	},

	// template to override in subclass
	beforeRequest: Ext.emptyFn
});