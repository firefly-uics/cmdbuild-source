(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.email.Step3View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.common.NotificationForm',
			'CMDBuild.proxy.administration.taskManager.task.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Step3}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldAttachmentsCategory: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldParsingKeyStart: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldParsingKeyEnd: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldParsingValueStart: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldParsingValueEnd: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldNotificationTemplate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetAttachments: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetNotification: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetParsing: undefined,

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
					this.fieldsetParsing = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.bodyParsing,
						checkboxName: CMDBuild.core.constants.Proxy.PARSING_ACTIVE,
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
							{
								xtype: 'container',
								padding: '0 0 5 0',

								layout: {
									type: 'hbox',
									align: 'stretch'
								},

								items: [
									this.fieldParsingKeyStart = Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.PARSING_KEY_INIT,
										fieldLabel: CMDBuild.Translation.keyStartDelimeter,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
										flex: 1
									}),
									this.fieldParsingKeyEnd = Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.PARSING_KEY_END,
										fieldLabel: CMDBuild.Translation.keyEndDelimiter,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
										margin: '0 0 0 20',
										flex: 1
									})
								]
							},
							{
								xtype: 'container',
								padding: '0 0 5 0',

								layout: {
									type: 'hbox',
									align: 'stretch'
								},

								items: [
									this.fieldParsingValueStart = Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT,
										fieldLabel: CMDBuild.Translation.valueStartDelimeter,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
										flex: 1
									}),
									this.fieldParsingValueEnd = Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.PARSING_VALUE_END,
										fieldLabel: CMDBuild.Translation.valueEndDelimiter,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
										margin: '0 0 0 20',
										flex: 1
									})
								]
							}
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEmailStep3FieldsetParsingExpand');
							}
						}
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
							this.fieldNotificationTemplate = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE,
								fieldLabel: CMDBuild.Translation.template,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
								valueField: CMDBuild.core.constants.Proxy.NAME,
								displayField: CMDBuild.core.constants.Proxy.NAME,
								forceSelection: true,

								store: CMDBuild.proxy.administration.taskManager.task.Email.getStoreTemplate(),
								queryMode: 'local'
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEmailStep3FieldsetNotificationExpand');
							}
						}
					}),
					this.fieldsetAttachments = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.saveAttachmentsToDms,
						checkboxName: CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE,
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
							this.fieldAttachmentsCategory = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY,
								fieldLabel: CMDBuild.Translation.category,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
								displayField: 'Description',
								valueField: 'Id',
								forceSelection: true,

								store: CMDBuild.proxy.administration.taskManager.task.Email.getStoreLokup(),
								queryMode: 'local'
							})
						],

						listeners: {
							scope: this,
							expand: function (fieldset, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEmailStep3FieldsetAttachmentsExpand');
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
