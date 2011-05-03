(function() {
/* Add to form the methods for hiding a form field with its label */
Ext.override(Ext.form.Field, {
	
	hide: function() {
	Ext.form.Field.superclass.hide.call(this);
		if (this.cmdb_hideWithContainer) {		
			this.getEl().up('.x-form-item').setDisplayed(false); // hide container and children (including label if applicable)
		}
		
	},
	
	show: function() {
		if (this.cmdb_hideWithContainer) {
			this.getEl().up('.x-form-item').setDisplayed(true); // hide container and children (including label if applicable)
		}
		Ext.form.Field.superclass.show.call(this);
	},
	
    showContainer: function() {
        this.enable();
        this.show();
        this.getEl().up('.x-form-item').setDisplayed(true); // show entire container and children (including label if applicable)
    },

    hideContainer: function() {
        this.disable(); // for validation
        this.hide();
        if (this.getEl()) {
        	this.getEl().up('.x-form-item').setDisplayed(false); // hide container and children (including label if applicable)
    	}
    },

    isContainerVisible: function() {
        return this.getEl().up('.x-form-item').isVisible();
    },
    
    setContainerVisible: function(visible) {
        if (visible) {
            this.showContainer();
        } else {
            this.hideContainer();
        }
        return this;
    }
    
});  

Ext.override(Ext.form.FormPanel, {
	getInvalidFieldsAsHTML: function() {
		var BEGIN_LIST = "<ul>";
		var END_LIST = "</ul>";
		var out = "";
		this.cascade(function(item) {
			if (item && (item instanceof Ext.form.Field)) {
				if (!item.isValid()) {
					out += "<li>" + item.fieldLabel + "</li>";
				}
			}
		});
		if (out == "") {
			return null;
		} else { 
			return BEGIN_LIST + out + END_LIST;
		}
	},	
	
	isReadOnly: function(field) {
		if (field) {
			return field.initialConfig.CMDBuildReadonly;
		} else {
			return false;
		}
	},

	setFieldsEnabled: function(enableAll) {
		if (!this.MODEL_STRUCTURE) {
			return setFieldsEnabledForLegacyCode.call(this, enableAll);
		}

		var s = this.MODEL_STRUCTURE;
		this.cascade(function(item) {
			if (item && (item instanceof Ext.form.Field)) {
				var name = item._name || item.name; // for compatibility I can not change the name of old attrs
				var toBeEnabled = enableAll || !s[name].immutable;
				if (toBeEnabled) {
					item.enable();
				}
			}
		});

	},

	setFieldsDisabled: function(){
		this.stopMonitoring();
		if (!this.MODEL_STRUCTURE) {
			setFieldsDisabledForLegacyCode.call(this);
		} else {
			var s = this.MODEL_STRUCTURE;
			this.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field) && item.disable) {
					item.disable();
				}
			});
		}
	},

	enableAllField: function() {
		var fields = this.getForm().items.items;
		this.formIsDisable = false;
		for (var i = 0 ; i < fields.length ; i++) {
			fields[i].enable();
		}
	},
	
	disableAllField: function() {
		var fields = this.getForm().items.items;
		this.formIsDisable = true;
		for (var i = 0 ; i < fields.length ; i++) {
			fields[i].disable();
		}
	},
	
	forEachField: function(fn) {
		var fields = this.getForm().items;
		for (var i = 0 ; i < fields.length ; i++) {
			var field = fields[i];
			fn(field);
		} 
	}
}); 

// http://extjs.com/forum/showthread.php?t=43356
Ext.override(Ext.form.Checkbox, {
	getValue : function(){
		if(this.rendered){
			return this.el.dom.checked;
		}
		return this.checked;
	},

	setValue : function(v) {
		var checked = this.checked;
		this.checked = (v === true || v === 'true' || v == '1' || String(v).toLowerCase() == 'on');

		if(this.rendered){
			this.el.dom.checked = this.checked;
			this.el.dom.defaultChecked = this.checked;
			this.wrap[this.checked? 'addClass' : 'removeClass'](this.checkedCls);
		}

		if(checked != this.checked){
			this.fireEvent("check", this, this.checked);
			if(this.handler){
				this.handler.call(this.scope || this, this, this.checked);
			}
		}
	}
});

Ext.override(Ext.form.Hidden, {
	validateValue: function(value) {
		if (this.allowBlank === false) {
			return (value.length > 0);
		}
		return true;
	}
});

Ext.override(Ext.form.BasicForm, {
	//to fire an event with the record loaded
	loadRecord: function(record) {
		this.setValues(record.data);
		this.fireEvent('loadrecord', {
			record: record,
			form: this
		});
		return this;
	}
});

Ext.override( Ext.form.FieldSet, {
	syncSize: function() {
		Ext.form.FieldSet.superclass.syncSize.call(this);
		var items = this.items.items;
		for (var i=0; i<items.length; i++) {
			var item = items[i];
			if (item && item.syncSize) {
				item.syncSize();
			}
		}
	}
});

function setFieldsEnabledForLegacyCode(enableAll) {
	this.startMonitoring();
	this.cascade(function(item) {
	if (item && (item instanceof Ext.form.Field)
			&& item.isVisible() 
			&& (enableAll || !(item.initialConfig.CMDBuildReadonly)))
		item.enable();
	});
	if (this.buttons) {
		for(var i=0; i<this.buttons.length; i++ ){
			if (this.buttons[i]) {
					this.buttons[i].enable();
			}
		}
	}
}

function setFieldsDisabledForLegacyCode() {
	this.cascade(function(i) {
		if (i && (i instanceof Ext.form.Field) && !(i instanceof Ext.form.DisplayField)){
			var xtype = i.getXType();
			if (xtype!='hidden') {
				i.disable();
			}
		}
	});
	if (this.buttons) {
		for(var i=0; i<this.buttons.length; i++ ){
			if (this.buttons[i]) {
				this.buttons[i].disable();
			}
		}
	}
}

})();