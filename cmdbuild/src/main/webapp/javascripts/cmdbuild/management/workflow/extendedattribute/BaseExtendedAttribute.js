CMDBuild.Management.BaseExtendedAttribute = Ext.extend(Ext.Panel, {
	clientForm: undefined, // passed to extattr definition in OptionPanel
	identifier: '',
	bottomButtons: [],
	required: false,
	activity: undefined,
	tabpanel_id: 'activityopts_tab',
	busy: false, 
	
	initComponent: function() {
		this.identifier = this.extAttrDef.identifier;
		this.required = (this.extAttrDef.required || this.extAttrDef.Required) ? true : false;
		var exts = this.initialize( this.extAttrDef );
		exts = exts || {};
		
		Ext.apply(
		  this,
		  exts,
		  {
		  	title: this.extAttrDef.btnLabel,
		  	frame: false,
		  	bodyBorder: false,
		  	border: false,
		  	layout: 'fit',
		  	style: {background: CMDBuild.Constants.colors.blue.background},
		  	xtype: 'panel',
		  	hideMode: 'offsets',
		  	buttonAlign: 'center',
		    buttons: this.buildButtonsArray()
		  }
		);
		
		CMDBuild.Management.BaseExtendedAttribute.superclass.initComponent.apply(this, arguments);
		
		this.publish('cmdb-extattr-instanced', {identifier:this.identifier,bottomButtons:exts.btmButtons});
		this.on('hide', this.onDeactivation, this);
	},

	buildButtonsArray: function() {
		var buttonsArray = [];
		this.backButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.workflow.back,
			handler: this.backToActivityTab,
			scope: this
		});
		buttonsArray[0] = this.backButton;
		buttonsArray = this.buildSpecificButtons().concat(buttonsArray);
		return buttonsArray;
	},
	
	//this method can be overwrite in the subclass to add the needed buttons BAZINGA
	buildSpecificButtons: function() {		
		return [];
	},
	
	backToActivityTab: function(evt) {
        this.findParentByType('activitytabpanel').setActiveTab('activity_tab');
    },
	
	getVariable: function(variableFullName) {
		return this.getTemplateResolver().getVariable(variableFullName);
	},

	resolveTemplates: function(options) {
		this.getTemplateResolver().resolveTemplates(options);
	},

	// private
	getTemplateResolver: function() {
		return this.templateResolver || new CMDBuild.Management.TemplateResolver({
			clientForm: this.clientForm,
			xaVars: this.extAttrDef,
			serverVars: this.activity
		});
	},

	onActivation: function() {
		CMDBuild.log.info('on show of extAttr: ' + this.identifier);
		this.setButtonsVisible(true);
		this.activateTab();
		this.showCardIfNeeded();
		this.onExtAttrShow(this);
	},
	
	showCardIfNeeded: function() {
		var tab = Ext.getCmp(this.tabpanel_id);
		if (tab.getLayout().setActiveItem) {
			tab.getLayout().setActiveItem(this.id);
		}
	},
	
	activateTab: function() {
		var tab = Ext.getCmp(this.tabpanel_id);
		tab.ownerCt.setActiveTab(this.tabpanel_id);
		tab.enable();
	},
	
	onDeactivation: function() {
		CMDBuild.log.info('on hide of extAttr: ' + this.identifier);
		this.setButtonsVisible(false);
		this.onExtAttrHide(this);
	},

	getProcessId: function() {
		return this.getVariable("server:Id");
	},

	setButtonsVisible: function(visible) {
        Ext.each(this.bottomButtons, function(btn) {
            btn.setVisible(visible);
        });
	},
	
	setup: function(procInstId, workItemId) {
		this.processInstanceId = procInstId;
		this.workItemId = workItemId;
	},
	
	react: function(data) {
		this.busy = true;
		this.savingWentWrong = false;
		
		data = Ext.apply(data,{
		  identifier: this.identifier,
		  ProcessInstanceId: this.processInstanceId,
		  WorkItemId: this.workItemId
		});
		
		Ext.Ajax.request({
			url : 'services/json/management/modworkflow/reactextendedattribute',
			method : 'POST',
			params : data,
			scope : this,
			failure : function() {
				this.savingWentWrong = true;
			},
			callback : function() {
				this.busy = false;
			}
		});
	},

	save: function() {
		if (this.onSave && this.isValid()) {
			this.onSave(this.afterSave);
		}
	},

	afterSave: function() {
		this.busy = false;
	},

	isBusy: function() {
		return this.busy;	
	},
	
	isValid: function() {
		var data = this.getData();
		if (this.required === true &&
				(Ext.isEmpty(data) || (Ext.isArray(data) && data.length == 0))) {
			_debug(this.extAttrDef.btnLabel + " is valid: ", false);
			return false;
		} else {
			_debug(this.extAttrDef.btnLabel + " is valid: ", true);
			return true;
		}
	},
	
	onActivityStartEdit: Ext.emptyFn,
	getData: Ext.emptyFn,
	initialize: Ext.emptyFn,
	onExtAttrHide: Ext.emptyFn,
	onExtAttrShow: Ext.emptyFn
});