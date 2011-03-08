CMDBuild.Administration.TemplateModSetup = Ext.extend(CMDBuild.ModPanel, {	
	//custom attributes
	addAction: {},
	removeAction: {},
	modifyAction: {},
	actions: [],
	formItems: [],
	configFileName: '',
	hideMode: 'offsets', //to fix the width of combolist

	initComponent: function() {
		var form = this;
		this.saveBtn = new Ext.Button({
    		xtype: 'button',
    		text: CMDBuild.Translation.common.buttons.save,
    		scope: this,
    		formBind: true,
    		handler: function(){
    			CMDBuild.LoadMask.get().show();
    			this.form.getForm().submit({
					scope: this,
					callback: function() {
						CMDBuild.LoadMask.get().hide();
						//needed to mantein the consistenece beetween the information displayed and the 
						//information in the config file
						form.getConfigurationFromServer();
						form.afterSubmit(arguments);
          			}
				});
    		}
    	});
    	
    	this.abortBtn = new Ext.Button({
    		text: CMDBuild.Translation.common.buttons.abort,
    		scope: this,
    		handler: function(){
    			this.form.getForm().reset();
    		}
    	});
    	
		this.form = new Ext.form.FormPanel({
			region: 'center',			
			trackResetOnLoad: true,
			plugins: new CMDBuild.CallbackPlugin(),			
			url : 'services/json/schema/setup/saveconfiguration',
			baseParams: {
				name: this.configFileName
			},
			monitorValid: true,
			labelWidth: 300,
			defaultType : 'textfield',
			items: [this.formItems],
			buttonAlign: 'center',
			buttons: [this.saveBtn, this.abortBtn],
			autoScroll: true
		});
		
		this.getForm = function() {
			return this.form.getForm();
		};
		
		Ext.apply(this, {
			border: true,
			frame: true,
			layout: 'border',
			items:[this.form]
    	});
    	
    	CMDBuild.Administration.TemplateModSetup.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-select-'+this.modtype, this.getConfigurationFromServer, this);
    	this.form.on('clientvalidation', function(panel, valid) {
    		this.saveBtn.setDisabled(!valid);
    	}, this);
	},
	
	getConfigurationFromServer: function(){
		CMDBuild.Ajax.request({
			url : String.format('services/json/schema/setup/getconfiguration'),
			params: {
				name: this.configFileName
			},
			success: function(response){
				this.populateForm(Ext.util.JSON.decode(response.responseText));			
				this.publish('cmdb-config-update-'+this.configFileName, this.form.getForm().getValues());
			},
			scope: this
		});
	},
	
	populateForm: function(configurationOptions){
		var valuesFromServer = configurationOptions.data;
		this.form.getForm().setValues(valuesFromServer);
	},
	
	/**
	 * Template method called in the
	 * callbak function of the form submit
	 **/
	afterSubmit: function() {
		_debug("before submit of the templateModSetuo");
	}
});
