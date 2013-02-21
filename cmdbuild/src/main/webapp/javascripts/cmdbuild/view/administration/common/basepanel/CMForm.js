/**
 * @class CMDBuild.view.administration.common.basepanel.CMForm
 * 
 * The base of the new generation CMDBUild Form
 */
Ext.define("CMDBuild.view.administration.common.basepanel.CMForm", {
	extend: "Ext.form.Panel",

	// configuration
	modifyButtonText: "Modify",
	removeButtonText: "Remove",
	// configuration

	mixins: {
		cmFormFunctions: "CMDBUild.view.common.CMFormFunctions",
		delegable: "CMDBuild.core.CMDelegable"
	},

	constructor: function() {
		this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.administration.common.basepanel.CMFormDelegate");

		this.callParent(arguments);
	},

	initComponent: function() {
		var me = this;

		this.modifyButton = new Ext.button.Button({
			iconCls: "modify",
			text: this.modifyButtonText,
			handler: function() {
				me.callDelegates("onFormModifyButtonClick", me);
			}
		});

		this.removeButton = new Ext.button.Button({
			iconCls: "delete",
			text: this.removeButtonText,
			handler: function() {
				me.callDelegates("onFormRemoveButtonClick", me);
			}
		});

		this.saveButton = new CMDBuild.buttons.SaveButton({
			handler: function() {
				me.callDelegates("onFormSaveButtonClick", me);
			}
		});

		this.abortButton = new CMDBuild.buttons.AbortButton({
			handler: function() {
				me.callDelegates("onFormAbortButtonClick", me);
			}
		});

		this.cmTBar = [this.modifyButton, this.removeButton];
		this.cmButtons = [this.saveButton, this.abortButton];
		this.buttonAlign = "center";
		this.buttons = this.cmButtons;
		this.tbar = this.cmTBar;
		this.autoScroll = true;

		this.callParent(arguments);
	},

	/**
	 * 
	 * @param {CMDBuild.delegate.administration.common.basepanel.CMFormFiledsManager} fieldManager
	 * Build the fields for this form, asking them to the given field manager
	 */
	buildFields: function(fieldManager) {
		if (fieldManager != null) {
			var fields = fieldManager.build();
			if (fields) {
				this.removeAll();
				this.add(fields);
			}
		}
	}
});