(function() {
	
Ext.ns("CMDBuild.administration.form");

CMDBuild.administration.form.CMFormTemplate = Ext.extend(Ext.Panel, {
	plugins : [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
	// define the following in the subclasses
	model: null,
	MODEL_STRUCTURE: null,
	formFields: [],
	modifyButtonLabel: "",
	deleteButtonLabel: "",
	
	initComponent: function() {
		
		this.saveButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.save,
			disabled: true
		});

		this.abortButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.abort,
			disabled: true,
			scope: this,
			handler: function() {
				this.clearForm();
				this.disableModify();

				if (this.model != null) {
					// refill with the model to discard the modifications
					this.fillWithModel(this.model);
				} else {
					// we can not modify or delete because there is not a selected model
					// this happens when abort the creation of a new domain
					this.disableToolBar();
				}
			}
		});

		this.modifyButton = new Ext.Button({
			iconCls: 'modify',
			text: this.modifyButtonLabel,
			disabled: true,
			scope: this,
			handler: function() {
				this.enableModify();
			}
		});

		this.deleteButton = new Ext.Button({
			iconCls: 'delete',
			text: this.deleteButtonLabel,
			disabled: true
		});

		this.formPanel = new Ext.form.FormPanel({
			MODEL_STRUCTURE: this.MODEL_STRUCTURE,
			defaultType: 'textfield',
			autoScroll: true,
			labelWidth: 150,
			frame: true,
			border: false,
			cls: CMDBuild.Constants.css.padding5,
			region: "center",
			items: this.formFields
		});
		
		this.layout = "border";
		this.frame = false;
		this.border = false;
		this.cls = CMDBuild.Constants.css.bg_gray;
		this.tbar = [this.modifyButton, this.deleteButton];
		this.items = [this.formPanel];
		this.buttonAlign = "center";
		this.buttons = [this.saveButton, this.abortButton];
		this.bodyCssClass = CMDBuild.Constants.css.bg_gray;
		
		CMDBuild.administration.form.CMFormTemplate.superclass.initComponent.apply(this, arguments);
	},

	getForm: function() {
		return this.formPanel.getForm();
	},

	getInvalidFieldsAsHTML: function() {
		return this.formPanel.getInvalidFieldsAsHTML();
	},

	clearForm : function() {
		this.getForm().reset();
	},

	destroyModel: function() {
		this.model.destroy();
		this.clearForm();
		this.model = null;
	},

	prepareToAdd: function() {
		this.enableModify(all=true);
		this.clearForm();
		this.model = null;
		this.setDefaultValues();
	},
	
	// template for subclasses
	setDefaultValues: Ext.emptyFn,

	enableModify: function(all) {
		if (all) {
			this.formPanel.enableAllField();
		} else {
			this.formPanel.setFieldsEnabled();
		}
		this.saveButton.enable();
		this.abortButton.enable();
		
		this.disableToolBar();
	},

	disableModify: function() {
		this.formPanel.disableAllField();
		this.saveButton.disable();
		this.abortButton.disable();
		
		this.enableToolBar();
		this.getForm().clearInvalid();
	},

	fillWithModel: function(cmdomain) {
		if (cmdomain.NAME != CMDBuild.core.model.CMDomainModel.NAME) {
			throw CMDBuild.core.error.form.WRONG_MODEL(CMDBuild.core.model.CMDomainModel.NAME);
		} else {
			this.model = cmdomain;
			var rec = cmdomain.getRecord();
			this.getForm().loadRecord(rec);
		}
	},

	enableToolBar: function() {
		setDisabledToolbarItems.call(this, disabled=false);
	},

	disableToolBar: function() {
		setDisabledToolbarItems.call(this, disabled=true);
	}
});

	// called with this as scope
	function setDisabledToolbarItems(disabled) {
		this.modifyButton.setDisabled(disabled);
		this.deleteButton.setDisabled(disabled);
	}

})();