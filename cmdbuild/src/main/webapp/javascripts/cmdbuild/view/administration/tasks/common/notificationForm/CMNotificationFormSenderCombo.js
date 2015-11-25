(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormSenderCombo', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.email.Accounts'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.tasks.notificationForm.account,
		name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT,

		valueField: CMDBuild.core.constants.Proxy.NAME,
		displayField: CMDBuild.core.constants.Proxy.NAME,
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