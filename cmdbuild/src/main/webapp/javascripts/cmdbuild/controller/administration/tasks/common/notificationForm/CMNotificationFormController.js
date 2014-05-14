(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');
	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define('CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController', {

//		senderCombo: undefined,
//		templateCombo: undefined,
		/**
		 * Array = [
		 * 		'internalId': { Input object },
		 * 		...
		 * ]
		 */
		inputFields: {},

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

		isEmptySenderCombo: function() {
			if (!Ext.isEmpty(this.senderCombo))
				return Ext.isEmpty(this.senderCombo.getValue());

			return true;
		},

		isEmptyTemplateCombo: function() {
			if (!Ext.isEmpty(this.templateCombo))
				return Ext.isEmpty(this.templateCombo.getValue());

			return true;
		},

		/**
		 * Set fields as required/unrequired
		 *
		 * @param (Boolean) state
		 */
		setAllowBlankFields: function(state) {
			if (!Ext.isEmpty(this.senderCombo))
				this.senderCombo.allowBlank = state;

			if (!Ext.isEmpty(this.templateCombo))
				this.templateCombo.allowBlank = state;
		},

		/**
		 * @param (String) value
		 */
		setValueSender: function(value) {
			if (!Ext.isEmpty(value)) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.senderCombo.forceSelection = false;
				this.senderCombo.setValue(value);
				this.senderCombo.forceSelection = true;
			}
		},

		/**
		 * @param (String) value
		 */
		setValueTemplate: function(value) {
			if (!Ext.isEmpty(value)) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.templateCombo.forceSelection = false;
				this.templateCombo.setValue(value);
				this.templateCombo.forceSelection = true;
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
			if (
				this.isEmptySenderCombo()
				&& this.isEmptyTemplateCombo()
				&& enable
			) {
				this.setAllowBlankCombo(false);
			} else {
				this.setAllowBlankCombo(true);
			}
		}
	});

})();