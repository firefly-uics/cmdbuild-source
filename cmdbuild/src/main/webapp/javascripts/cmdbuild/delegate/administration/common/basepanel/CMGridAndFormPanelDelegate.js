/**
 * Give a base implementation of the delegates
 * 	CMDBuild.delegate.administration.common.basepanel.CMFormDelegate
 * 	CMDBuild.delegate.administration.common.basepanel.CMGridDelegate
 * 
 * and add his own method that are called form a CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel
 */

Ext.define("CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate", {

	mixins: {
		formDelegate: "CMDBuild.delegate.administration.common.basepanel.CMFormDelegate",
		gridDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridDelegate"
	},

	constructor: function(view) {
		view.addDelegate(this);
		view.form.addDelegate(this);
		view.grid.addDelegate(this);
	},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel} panel
	 * called from the panel after a click on the add button
	 */
	onGridAndFormPanelAddButtonClick: function(panel) {
		var all = true;
		this.record = null;
		this.fieldManager.reset();
		panel.enableModify(all);
		panel.clearSelection();
	},

	/**
	 * called after the save button click
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	onGridAndFormPanelSaveButtonClick: function(form) {},

	/**
	 * called after the confirmation of a remove
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	onGridAndFormPanelRemoveConfirmed: function(form) {},

	// as form delegate

	onFormModifyButtonClick: function(form) {
		this.view.enableModify();
	},

	onFormRemoveButtonClick: function(form) {
		var me = this;
		Ext.Msg.show({
			title: "@@ Attenzione",
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					me.onGridAndFormPanelRemoveConfirmed(form);
					me.view.disableModify();
				}
			}
		});
	},

	onFormSaveButtonClick: function(form) {
		this.view.disableModify();
		this.onGridAndFormPanelSaveButtonClick(form);
	},

	onFormAbortButtonClick: function(form) {
		if (this.record) {
			this.fieldManager.loadRecord(this.record);
		} else {
			this.fieldManager.reset();
		}

		this.view.disableModify();
	},

	// as grid delegate

	onCMGridSelect: function(grid, record) {
		this.record = record;
		var enableToolbar = !!record;
		this.view.disableModify(enableToolbar);
		if (this.fieldManager) {
			this.fieldManager.loadRecord(record);
		}
	}
});