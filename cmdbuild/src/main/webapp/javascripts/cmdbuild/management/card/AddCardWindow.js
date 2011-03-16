CMDBuild.Management.AddCardWindow = Ext.extend(CMDBuild.PopupWindow, {
	card: undefined,
	withButtons: true,
	withToolBar: false,
	cancelButtonHandler: undefined,
	saveButtonHandler: undefined,
		
    initComponent:function() {
		var _this = this;
		this.saveButtonHandler = function() {
			var form = _this.cardForm.form.getForm();
			if (form.isValid()) {
				form.submit({
					method : 'POST',
					url : 'services/json/management/modcard/updatecard',				
					scope: _this,
					success : function(form, action) {
						this.fireEvent('cmdbuild-add-card');
						this.close();
					}					
				});
			}
		},
		this.cancelButtonHandler = function() {
			_this.close();
		};
	
		this.cardForm = new CMDBuild.Management.CardTabUI( {
			withButtons: this.withButtons,
			withToolBar: this.withToolBar,
	        subscribeToEvents: false,
	        autoScroll: true,
	        cancelButtonHandler: this.cancelButtonHandler,
	    	saveButtonHandler: this.saveButtonHandler,
	    	allowNoteFiled: true
	    });
			
		if (!this.withButtons) {
			this.closeButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.close,
		        name: "cancelButton",
		        handler: this.close,
		        scope: this
			});
			this.buttons = [this.closeButton];
		}

        Ext.apply(this, {
        	title: this.className,
	        items: this.cardForm,
	        buttonAlign: "center"	        
        });
		CMDBuild.Management.AddCardWindow.superclass.initComponent.apply(this);
		this.on("show", this.loadCard, this);
		this.cardForm.on("cmdb-close-window", this.close, this);
	},	
	
	loadCard: function() {
		var callback = (function(attributes) {
			this.cardForm.buildTabbedPanel(attributes);
			this.cardForm.newCard({
				classId: this.classId	
			});
		}).createDelegate(this);
		
		CMDBuild.Management.FieldManager.loadAttributes(this.classId,callback);
	}
});