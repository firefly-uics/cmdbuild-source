(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormSenderCombo', {
		extend: 'Ext.form.field.ComboBox',

		// Required
		delegate: undefined,
		fieldLabel: CMDBuild.Translation.administration.email.accounts.account,
		name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT,

		valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
		displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
		store: CMDBuild.core.proxy.CMProxyEmailAccounts.getStore(),
		labelWidth: CMDBuild.LABEL_WIDTH,
		width: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false
	});

})();