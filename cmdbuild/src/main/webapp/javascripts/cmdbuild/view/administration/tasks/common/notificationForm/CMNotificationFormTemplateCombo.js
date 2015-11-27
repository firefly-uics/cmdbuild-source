(function() {

	// Here because requires property doesn't work
	Ext.require('CMDBuild.core.constants.Proxy');
	Ext.require('CMDBuild.core.proxy.email.Template');

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormTemplateCombo', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.tasks.notificationForm.template,
		name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE,

		valueField: CMDBuild.core.constants.Proxy.NAME,
		displayField: CMDBuild.core.constants.Proxy.NAME,
		labelWidth: CMDBuild.LABEL_WIDTH,
		maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false,
		anchor: '100%',

		store: CMDBuild.core.proxy.email.Template.getStore()
	});

})();