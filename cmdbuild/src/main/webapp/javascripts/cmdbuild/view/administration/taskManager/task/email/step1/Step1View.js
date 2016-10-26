(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.email.step1.Step1View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Step1}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.email.step1.FilterFieldset}
		 */
		fieldsetFilter: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldEmailAccount: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldFolderIncoming: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldFolderProcessed: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldFolderRejected: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetRejected: undefined,

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
						value: CMDBuild.Translation.email
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
					this.fieldEmailAccount = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT,
						fieldLabel: CMDBuild.Translation.emailAccount,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,

						store: CMDBuild.proxy.administration.taskManager.task.Email.getStoreAccount(),
						queryMode: 'local'
					}),
					this.fieldFolderIncoming = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.INCOMING_FOLDER,
						fieldLabel: CMDBuild.Translation.incomingFolder,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					}),
					this.fieldsetFilter = Ext.create('CMDBuild.view.administration.taskManager.task.email.step1.FilterFieldset', { delegate: this.delegate }),
					this.fieldFolderProcessed = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.PROCESSED_FOLDER,
						fieldLabel: CMDBuild.Translation.processedFolder,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					}),
					this.fieldsetRejected = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.enableMoveRejectedNotMatching,
						checkboxName: CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING,
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
							this.fieldFolderRejected = Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.REJECTED_FOLDER,
								fieldLabel: CMDBuild.Translation.rejectedFolder,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 10
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEmailStep1FieldsetRejectedExpand');
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
