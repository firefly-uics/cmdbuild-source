(function() {
	var buttonTr = CMDBuild.Translation.common.buttons;
	
	Ext.define("CMDBuild.view.administration.configuration.CMBaseModConfiguration", {
		extend: "Ext.form.Panel",

		constructor: function() {
			this.saveButton = new Ext.Button({
				text: buttonTr.save
			});
			
			this.abortButton = new Ext.Button({
				text: buttonTr.abort
			});
			
			this.buttons = [this.saveButton, this.abortButton];
			this.frame = true;
			this.autoScroll = true;
			this.fieldDefaults = {
				labelAlign: 'left',
				labelWidth: 200
			};
			
			this.callParent(arguments);
		},
		
		getValues: function() {
			return this.getForm().getValues();
		},
		
		populateForm: function(configurationOptions) {
			var valuesFromServer = configurationOptions.data;
			this.getForm().setValues(valuesFromServer);
		},
		
		/**
		 * Template method called in the
		 * callbak function of the form submit
		 **/
		afterSubmit: function() {
			_debug("before submit of the templateModSetuo");
		}
	});

})();