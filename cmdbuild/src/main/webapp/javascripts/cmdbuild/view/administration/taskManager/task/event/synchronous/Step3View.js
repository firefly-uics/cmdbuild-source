(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.event.synchronous.Step3View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.event.Synchronous'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step3}
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

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetWorkflow: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView}
		 */
		fieldWorkflow: undefined,

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

								store: CMDBuild.proxy.administration.taskManager.task.event.Synchronous.getStoreAccount(),
								queryMode: 'local'
							}),
							this.fieldNotificationTemplate = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE,
								fieldLabel: CMDBuild.Translation.template,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								displayField: CMDBuild.core.constants.Proxy.NAME,
								valueField: CMDBuild.core.constants.Proxy.NAME,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								forceSelection: true,

								store: CMDBuild.proxy.administration.taskManager.task.event.Synchronous.getStoreTemplate(),
								queryMode: 'local'
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEventSynchronousStep3FieldsetNotificationExpand');
							}
						}
					}),
					this.fieldsetWorkflow = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.startProcess,
						checkboxName: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE,
						checkboxToggle: true,
						checkboxUncheckedValue: false,
						checkboxValue: true,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,
						overflowY: 'auto',

						items: [
							this.fieldWorkflow = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView', {
								name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,

								config: {
									comboName: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME,
									gridName: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES
								}
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEventSynchronousStep3FieldsetWorkflowExpand');
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
