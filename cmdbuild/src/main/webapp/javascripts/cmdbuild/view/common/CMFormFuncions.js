(function() {
	Ext.define("CMDBUild.view.common.CMFormFunctions", {
		enableFields: function(enableAll) {
			this.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field)) {
					var name = item._name || item.name;// for compatibility I can not change the name of old attrs
					var toBeEnabled = enableAll || !item.cmImmutable;
					if (toBeEnabled) {
						item.enable();
					}
				}
			});
		},

		disableFields: function(enableAll) {
			this.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field)) {
					item.disable();
				}
			});
		},

		enableCMButtons: function() {
			this.iterateOverCMButtons(function(b) {
				if (b && b.enable) {
					b.enable();
				}
			});
		},

		disableCMButtons: function() {
			this.iterateOverCMButtons(function(b) {
				if (b && b.disable) {
					b.disable();
				}
			});
		},
		
		disableCMTbar: function() {
			this.iterateOverCMTBar(function(i) {
				if (i && i.disable) {
					i.disable();
				}
			});
		},
		
		enableCMTbar: function() {
			this.iterateOverCMTBar(function(i) {
				if (i && i.enable) {
					i.enable();
				}
			});
		},

		enableModify: function(all) {
			this.enableFields(all);
			this.disableCMTbar();
			this.enableCMButtons();
		},

		disableModify: function(enableCMTBar) {
			this.disableFields();
			this.disableCMButtons();
			if (enableCMTBar) {
				this.enableCMTbar();
			} else {
				this.disableCMTbar();
			}
		},
		
		getData: function() {
			return this.getForm().getValues();
		},
		
		reset: function() {
			this.getForm().reset();
		},
		
		iterateOverCMButtons: function(fn) {
			this.iterateOverArray(this.cmButtons, fn);
		},
		
		iterateOverCMTBar: function(fn) {
			this.iterateOverArray(this.cmTBar, fn);
		},
		
		iterateOverArray: function(array, fn) {
			for (var i=0, l=array.length; i<l; ++i) {
				var x = array[i];
				fn(x);
			}
		}
	});
})();