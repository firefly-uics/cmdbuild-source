(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.connector.Step1View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Connector'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Step1}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldNotificationAccount: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldNotificationTemplate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetNotification: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.Display', {
						name: CMDBuild.core.constants.Proxy.TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						value: CMDBuild.Translation.connector
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.startOnSave,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.fieldsetNotification = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.sendNotificationEmail,
						checkboxName: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE,
						checkboxToggle: true,
						checkboxUncheckedValue: false,
						checkboxValue: true,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,
						overflowY: 'auto',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							this.fieldNotificationAccount = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT,
								fieldLabel: CMDBuild.Translation.account,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								displayField: CMDBuild.core.constants.Proxy.NAME,
								valueField: CMDBuild.core.constants.Proxy.NAME,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								forceSelection: true,

								store: CMDBuild.proxy.administration.taskManager.task.Connector.getStoreAccount(),
								queryMode: 'local'
							}),
							this.fieldNotificationTemplate = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE_ERROR,
								fieldLabel: CMDBuild.Translation.template,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								displayField: CMDBuild.core.constants.Proxy.NAME,
								valueField: CMDBuild.core.constants.Proxy.NAME,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								forceSelection: true,

								store: CMDBuild.proxy.administration.taskManager.task.Connector.getStoreTemplate(),
								queryMode: 'local'
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskConnectorStep1FieldsetNotificationExpand');
							}
						}
					}),
					Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
				]
			});

			this.callParent(arguments);
		}
	});

})();
