Ext.define("CMDBuild.view.administration.common.CMGridAndFormPanel", {
	extend: "Ext.panel.Panel",

	mixins: {
		delegable: "CMDBuild.core.CMDelegable"
	},

	constructor: function() {
		this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate");

		this.callParent(arguments);
	},

	// configuration
	addButtonText: "Add",
	modifyButtonText: "Modify",
	removeButtonText: "Remove",
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

		_CMUtils.forwardMethods(this, this.form, ["buildFields", "disableModify", "enableModify"]);
		_CMUtils.forwardMethods(this, this.grid, ["configureGrid"]);
	},

	buildGrid: function() {
		var grid = new CMDBuild.view.administration.common.basepanel.CMGrid({
			region: "center",
			border: false,
			frame: false
		});

		return grid;
	},

	buildForm: function() {
		var form = new CMDBuild.view.administration.common.basepanel.CMForm({
			modifyButtonText: this.modifyButtonText,
			removeButtonText: this.removeButtonText,

			region: "south",
			height: "70%",
			split: true,
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed cmbordertop",
			bodyCls: 'cmgraypanel'
		});

		return form;
	}
});