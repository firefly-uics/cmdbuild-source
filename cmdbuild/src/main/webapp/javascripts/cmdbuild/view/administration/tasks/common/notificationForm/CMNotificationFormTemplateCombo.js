(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormTemplateCombo', {
		extend: 'Ext.form.field.ComboBox',

		// Required
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.email.templates.template,
		name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE,
		valueField: CMDBuild.ServiceProxy.parameter.NAME,
		displayField: CMDBuild.ServiceProxy.parameter.NAME,
		store: CMDBuild.core.proxy.CMProxyEmailTemplates.getStore(),
		labelWidth: CMDBuild.LABEL_WIDTH,
//		width: CMDBuild.ADM_BIG_FIELD_WIDTH,
		width: (CMDBuild.ADM_BIG_FIELD_WIDTH - CMDBuild.LABEL_WIDTH - 5), // FIX: To solve a problem of width
		forceSelection: true,
		editable: false,

		initComponent: function() {
			this.callParent(arguments);
		}
	});

})();