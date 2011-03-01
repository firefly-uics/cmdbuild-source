CMDBuild.Management.AddAttachmentWindow = Ext.extend(CMDBuild.PopupWindow, {
	translation: CMDBuild.Translation.management.modcard.add_attachment_window,
	frame: true,
	initComponent: function() {

		this.confirmBtn = new CMDBuild.buttons.ConfirmButton({			
			scope: this,
			disabled: true,
			handler: this.onConfirm
    	});
		
		this.abortBtn = new CMDBuild.buttons.AbortButton({
			scope: this,			
			handler: function(){ this.close();}
    	});

		this.store = CMDBuild.Cache.getLookupStore(CMDBuild.Config.dms['category.lookup']);

		this.combo = new Ext.form.ComboBox({
			fieldLabel: this.translation.category,
			name: 'CategoryDescription',
			hiddenName: 'Category',
			store: this.store,
			valueField: CMDBuild.ServiceProxy.LOOKUP_FIELDS.Description,
			displayField: CMDBuild.ServiceProxy.LOOKUP_FIELDS.Description,
			triggerAction: 'all',
			allowBlank: false,
			forceSelection: true,
			mode: 'remote',
			emptyText: this.translation.select_category,
			width: 300
		});		
    	
    	this.form = new Ext.form.FormPanel({
    		autoHeight: true,
    		autoWidth: true,
    		encoding: 'multipart/form-data', 
    		fileUpload:true,
    		method: 'POST',
    		frame: false,
    		border: false,
    		bodyCssClass: "cmdbuild_background_blue cmdbuild_body_padding",
    		url : 'services/json/management/importcsv/uploadcsv',  
    		monitorValid: true,
    		id: 'form',			
			labelWidth: 200,
			items: [{
				xtype: 'hidden',
				name: 'IdClass',
				value: this.classId
			},{
				xtype: 'hidden',
				name: 'Id',
				value: this.cardId
			}, this.combo,
			{
				xtype: 'textfield',
				inputType : "file",					
				width: 230,
	    		fieldLabel: this.translation.load_attachment,
	    		allowBlank: false,
	    		name: 'File'    	
			},{
				xtype: 'textarea',
	 			fieldLabel: this.translation.description,
	    		name: 'Description',
	    		allowBlank: false,
	    		width: 300
			}]
    	});
    	
    	this.form.on('clientvalidation', function(form, valid){
    		this.confirmBtn.setDisabled(!valid);
    	}, this);
    	    	
    	Ext.apply(this, {
    		autoHeight: true,
    		autoWidth: true,
    		layout: 'fit',
    		title: this.translation.window_title,	    		
        	items: [this.form],				
			buttonAlign: 'center',
			buttons: [this.confirmBtn, this.abortBtn]
    	});
    	
    	this.on('show', function() {
    		this.form.getForm().isValid();
    	}, this);
    	CMDBuild.Management.AddAttachmentWindow.superclass.initComponent.apply(this, arguments);
    },
        
    onConfirm: function() {
    	this.form.getForm().submit({
    		method: 'POST',
    		url: 'services/json/management/modcard/uploadattachment',
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,		
			scope: this,
			success: function() {
				// Defer the call because Alfresco is not responsive
	        	function deferredCall() {
	                this.fireEvent('saved');
	                this.close();
	        	};
	        	deferredCall.defer(CMDBuild.Config.dms.delay, this);
	        },
	        failure: function () {
	        	this.enable();
	        }
    	});
    }
});