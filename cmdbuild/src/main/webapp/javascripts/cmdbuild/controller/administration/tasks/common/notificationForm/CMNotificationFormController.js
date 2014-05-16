(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');
	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define('CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController', {

		/**
		 * Array = [
		 * 		'internalId': { Input object },
		 * 		...
		 * ]
		 */
		inputFields: [],

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		isEmpty: function() {
			var returnBoolean = true;

			for (field in this.inputFields) {
				if (!Ext.isEmpty(this.inputFields[field]))
					returnBoolean = Ext.isEmpty(this.inputFields[field].getValue());
			}

			return returnBoolean;
		},

		/**
		 * Set fields as required/unrequired
		 *
		 * @param (Boolean) state
		 */
		setAllowBlankFields: function(state) {
			for (field in this.inputFields[field]) {
				if (!Ext.isEmpty(this.inputFields[field]))
					this.inputFields[field].allowBlank = state;
			}
		},

		/**
		 * @param (String) value
		 */
		setValue: function(internalId, value) {
			var inputField = this.inputFields[internalId];

			if (!Ext.isEmpty(inputField) && !Ext.isEmpty(value)) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				inputField.forceSelection = false;
				inputField.setValue(value);
				inputField.forceSelection = true;
			}
		},

		/**
		 * Notification form validation
		 *
		 * @param (Boolean) enable
		 *
		 * @return (Boolean)
		 */
		validate: function(enable) {
			if (this.isEmpty() && enable) {
				this.setAllowBlankFields(false);
			} else {
				this.setAllowBlankFields(true);
			}
		}
	});

})();