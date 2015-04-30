(function() {

	// Here because requires property doesn't work
	Ext.require('CMDBuild.core.proxy.CMProxyConstants');
	Ext.require('CMDBuild.core.proxy.email.Templates');

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormTemplateCombo', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.tasks.notificationForm.template,
		name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE,

		valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
		displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
		labelWidth: CMDBuild.LABEL_WIDTH,
		maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false,
		anchor: '100%',

		store: CMDBuild.core.proxy.email.Templates.getStore()
	});

})();