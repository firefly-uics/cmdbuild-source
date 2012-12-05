(function() {
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


Ext.override(Ext.form.Hidden, {
	validateValue: function(value) {
		if (this.allowBlank === false) {
			return (value.length > 0);
		}
		return true;
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

Ext.override(Ext.form.field.ComboBox, {
	// Currently disable only the field body,
	// It seems to be corrected in next Extjs release
	disable : function(silent) {
		var me = this;
		Ext.form.field.Base.prototype.disable.call(this, arguments);
		try {
			me.triggerEl.fadeOut();
		} catch (e) {}
	},

	enable : function(silent) {
		var me = this;
		Ext.form.field.Base.prototype.enable.call(this, arguments);
		try {
			me.triggerEl.fadeIn();
		} catch (e) {}
	}
});

function setFieldsEnabledForLegacyCode(enableAll) {
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