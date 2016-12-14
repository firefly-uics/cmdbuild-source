(function () {

	Ext.define('CMDBuild.view.administration.gis.FormBasePanel', {
		extend: 'Ext.form.Panel',

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