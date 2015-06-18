(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormSenderCombo', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.email.Accounts'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.tasks.notificationForm.account,
		name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_ACCOUNT,

		valueField: CMDBuild.core.proxy.Constants.NAME,
		displayField: CMDBuild.core.proxy.Constants.NAME,
		labelWidth: CMDBuild.LABEL_WIDTH,
		maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false,
		anchor: '100%',

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.core.proxy.email.Accounts.getStore()
			});

			this.callParent(arguments);
		}
	});

})();