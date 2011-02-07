CMDBuild.Management.CardWindow = Ext.extend(CMDBuild.PopupWindow, {
	card: undefined,
	withButtons: false,
	withToolBar: false,
	allowNoteField: true,
	cancelButtonHandler: undefined,
	saveButtonHandler: undefined,
	
    initComponent:function() {
		var _this = this;
		this.saveButtonHandler = function() {
			var form = _this.cardForm.form.getForm();
			if (form.isValid()) {
				_this.createCard(form);
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
	    	allowNoteFiled: this.allowNoteField
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
		CMDBuild.Management.CardWindow.superclass.initComponent.apply(this);
		this.on("show", this.loadCard, this);
		this.cardForm.on("cmdb-close-window", this.close, this);
	},
	
	loadCard: function() {
		var r = this.buildRecord(this.cardData);
		this.cardForm.loadCard(this.classAttributes, {
			record:r,
			enableModify: this.withButtons
		});
	},
	
	buildRecord: function(data) {
		var fields = [];
		for (var i in data) {
			var field = {name: i, mapping: i};
			fields.push(field);
		}
		var recordTemplate = Ext.data.Record.create(fields);
		return new recordTemplate(data);
	},

	createCard: function(form) {
		var _this = this;
		form.submit({
			method : 'POST',
			url : 'services/json/management/modcard/updatecard',
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope: this,
			params: (function() {
				var params = {};
				if (_this.referenceToMaster) {
					params[_this.referenceToMaster.name] = _this.referenceToMaster.value;
				}
				return params;
			})(),
			success : function(form, action) {
				this.onCreateCard(action.result.id);
			}
		});
	},

	onCreateCard: function(newCardId) {
		this.notifyAndcloseWindow(newCardId);
	},

	notifyAndcloseWindow: function() {
		this.destroy();
	}
});
