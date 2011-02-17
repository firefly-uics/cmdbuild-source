CMDBuild.ExtendedFormPanel = Ext.extend(Ext.Panel, {
	frame: false,
	border: false,
	url: undefined,
	fields: [{}], //an array of fields to add in the form panel
	tbarActions: [], //an array of action to put in the topBar
	withSaveButton: true, //set false these flags to disable the associated widget
	withAbortButton: true,
	onSave: function(){},
	onAbort: function(){},
	layout: 'border',
	panelLayout: 'form',
	
	initComponent:function() {
		
		this.saveButton = new CMDBuild.buttons.SaveButton({
			handler: this.onSave,
			disabled: true,
			scope: this
		});
		
		this.abortButton = new CMDBuild.buttons.AbortButton({
			handler: this.onAbort,
			disabled: true,
			scope: this
		});
		
	    this.panel = this.createPanel();
		
		Ext.apply(this, {			
			items: [this.panel],
			buttonAlign: 'center',
			buttons: this.setButtons(),
			tbar: this.createTopBar()
		});
		
		CMDBuild.ExtendedFormPanel.superclass.initComponent.apply(this, arguments);		
	},
	
	disableActions: function(actions) {
		for (var i = 0, len = actions.length; i<len; i++) {
			if(actions[i])
				actions[i].disable();
		}
	},
	
	enableActions: function(actions) {
		for (var i = 0, len = actions.length; i<len; i++) {
			if(actions[i])
				actions[i].enable();
		}
	},

	disableAllActions: function() {
		this.disableActions(this.tbarActions);
	},
	
	enableAllActions: function() {
		this.enableActions(this.tbarActions);
	},
	
	enableAllFields: function() {
		var fields = this.getForm().items.items;
		this.formIsDisable = false;
		for (var i = 0 ; i < fields.length ; i++) {
			if(fields[i])
				fields[i].enable();
		}
	},
	
	disableAllFields: function() {
		var fields = this.getForm().items.items;
		this.formIsDisable = true;
		for (var i = 0 ; i < fields.length ; i++) {
			var xtype = fields[i].getXType();
			if(fields[i] && xtype!='hidden')
				fields[i].disable();
		}
	},
	
	enableNonReadOnlyFields: function(enableAll){
   		this.panel.cascade(function(item) {
    		if (item && (item instanceof Ext.form.Field) && 
    				!(item.initialConfig.CMDBuildReadonly) &&
    				item.isVisible())
				item.enable();
		});
    },
	
	onClientValidation: function(form, valid) {
		this.setDisabled(!valid);
	},
	
	setButtons: function() {
		var buttons = [];
		if (this.withSaveButton)
			buttons.push(this.saveButton)
		if (this.withAbortButton)
			buttons.push(this.abortButton)
		return buttons
	},
	
	createPanel: function() {
		var panel = new Ext.form.FormPanel({
			url: this.url,
	    	monitorValid: true,
			frame: true,
			border: true,
			style: {padding: '5px'},
			autoScroll: true,
			region: 'center',
			trackResetOnLoad: true,
	    	items: this.fields,
	    	layout: this.panelLayout,
	    	labelWidth: this.labelWidth || 100
		});
		if (this.reader) {
			Ext.apply(panel, {
				reader: this.reader
			});
		}
		return panel;
	},
	
	createTopBar: function() {
		var topBar = undefined;
		if (this.tbarActions.length > 0) {
			topBar = this.tbarActions;
		}
		return topBar
	},
	
	getForm: function() {
		return this.panel.getForm();
	},
	
	startMonitoring: function() {
		this.panel.on('clientvalidation', this.onClientValidation, this.saveButton);
	},
	
	stopMonitoring: function() {
		this.panel.un('clientvalidation', this.onClientValidation, this.saveButton);
	},
	
	disableSaveButtonAndStopMonitoring: function() {
		this.stopMonitoring();
		this.saveButton.disable();
	},
	
	disableButtons: function() {
		this.disableActions(this.buttons)
	},
	
	enableButtons: function() {
		this.enableActions(this.buttons)
	},
	
	//the names of that functions are the
	//ideal situation of use that:
	//if I want a form to modify a 
	//card call modifyForm...
	
	modifyForm: function() {
		this.enableNonReadOnlyFields();
		this.disableAllActions();
		this.enableButtons();
		this.startMonitoring();
	},
	
	newForm: function() {
		this.enableAllFields();
		this.disableAllActions();
		this.enableButtons();
		this.startMonitoring();
	},
	
	abortModification: function() {
		this.disableAllFields();
		this.enableAllActions();
		this.disableButtons();
		this.stopMonitoring();
	},
	
	initForm: function() {
		this.disableAllFields();
		this.disableAllActions();
		this.disableButtons();
		this.stopMonitoring();
		this.clearForm();
	},
	
	loadRecord: function(record) {
		this.panel.getForm().loadRecord(record);
	}
});