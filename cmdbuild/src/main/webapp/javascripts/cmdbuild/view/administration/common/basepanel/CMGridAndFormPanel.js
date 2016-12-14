Ext.define("CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel", {
	extend: "Ext.panel.Panel",

	requires: ['CMDBuild.core.Utils'],

	mixins: {
		delegable: "CMDBuild.core.CMDelegable"
	},

	constructor: function() {
		this.mixins.delegable.constructor.call(this, "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate");

		this.callParent(arguments);
	},

	// configuration
	addButtonText: "Add",
	modifyButtonText: "Modify",
	removeButtonText: "Remove",
	withRemoveButton: true,
	withEnableDisableButton: false,
	fileUpload: false,
	withPagingBar: true,
	// configuration

	initComponent : function() {
		var me = this;

		this.addButton = new Ext.Button({
			iconCls: 'add',
			text: this.addButtonText,
			handler: function() {
				me.callDelegates("onGridAndFormPanelAddButtonClick", me);
			}
		});

		this.grid = this.buildGrid();
		this.form = this.buildForm();

		this.tbar = [this.addButton];
		this.frame = false;
		this.border = true;
		this.layout = "border";
		this.items = [this.grid, this.form];

		this.callParent(arguments);

		CMDBuild.core.Utils.forwardMethods(this, this.form, ["buildFields", "disableModify", "enableModify", "updateEnableDisableButton"]);
		CMDBuild.core.Utils.forwardMethods(this, this.grid, ["configureGrid"]);
	},

	buildGrid: function() {
		var gridConfig = {
			region: "center",
			border: false,
			frame: false,
			withPagingBar: this.withPagingBar
		};

		if (this.withPagingBar) {
			gridConfig.cls = "cmdb-border-bottom";
		}

		return new CMDBuild.view.administration.common.basepanel.CMGrid(gridConfig);
	},

	buildForm: function() {
		var form = new CMDBuild.view.administration.common.basepanel.CMForm({
			modifyButtonText: this.modifyButtonText,
			removeButtonText: this.removeButtonText,
			withRemoveButton: this.withRemoveButton,
			withEnableDisableButton: this.withEnableDisableButton,
			fileUpload: this.fileUpload,

			region: "south",
			height: "70%",
			split: true,
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed cmdb-border-top",
			bodyCls: 'cmdb-gray-panel'
		});

		return form;
	},

	clearSelection: function() {
		this.grid.getSelectionModel().deselectAll();
	},

	getBasicForm: function() {
		if (this.form) {
			return this.form.getForm();
		} else {
			return null;
		}
	}
});

// Legacy code
Ext.require(['CMDBuild.core.Message']);

/**
 * @class CMDBuild.delegate.administration.common.basepanel.CMGridDelegate
 *
 * Respond to the events fired from the Grid
 */
Ext.define("CMDBuild.delegate.administration.common.basepanel.CMGridDelegate", {
	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMGrid} grid
	 * the grid that calls this method
	 * @param {Ext.data.Model} record
	 * the selected record
	 */
	onCMGridSelect: function(grid, record) {}
});

/**
 * @class CMDBuild.delegate.administration.common.basepanel.CMFormDelegate
 *
 * Responds to the events fired from the Form
 */
Ext.define("CMDBuild.delegate.administration.common.basepanel.CMFormDelegate", {
	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormModifyButtonClick: function(form) {},

	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormRemoveButtonClick: function(form) {},

	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormSaveButtonClick: function(form) {},

	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormAbortButtonClick: function(form) {},

	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 *
	 * @param {String} action
	 * a string that say if the button is clicked when configured
	 * to activate or deactivate something ["disable" | "enable"]
	 */
	onEnableDisableButtonClick: function(form, action) {}

});

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
		this.view = view;
		view.addDelegate(this);
		view.form.addDelegate(this);
		view.grid.addDelegate(this);
	},

	selectFirstRow: function() {
		if (this.view.grid) {
			var store = this.view.grid.getStore();
			var sm = this.view.grid.getSelectionModel();

			if (store && sm) {
				var count = store.getTotalCount();
				if (count>0) {
					sm.select(store.getAt(0));
				}
			}
		}
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

	/**
	 *
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 *
	 * @param {String} action
	 * a string that say if the button is clicked when configured
	 * to activate or deactivate something ["enable" | "disable"]
	 */
	onEnableDisableButtonClick: function(form, action) {},

	// as form delegate

	onFormModifyButtonClick: function(form) {
		this.view.enableModify();
	},

	onFormRemoveButtonClick: function(form) {
		var me = this;
		Ext.Msg.show({
			title: CMDBuild.Translation.attention,
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
		var form = this.view.form.getForm();
		if (form && form.isValid()) {
			this.view.disableModify();
			this.onGridAndFormPanelSaveButtonClick(form);
		} else {
			CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
		}
	},

	onFormAbortButtonClick: function(form) {
		var enableCMTBar = false;
		if (this.record) {
			this.fieldManager.loadRecord(this.record);
			enableCMTBar = true;
		} else {
			this.fieldManager.reset();
		}

		this.view.disableModify(enableCMTBar);
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