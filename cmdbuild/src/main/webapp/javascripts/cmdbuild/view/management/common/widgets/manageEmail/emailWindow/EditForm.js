(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.emailWindow.EditForm', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMHtmlEditorField}
		 */
		emailContentField: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		keepSynchronizationCheckbox: undefined,

		frame: false,
		border: false,
		padding: '5',
		flex: 3,
		bodyCls: 'x-panel-body-default-framed',

		layout: {
			type: 'vbox',
			align: 'stretch' // Child items are stretched to full width
		},

		defaults: {
			labelAlign: 'right',
			labelWidth: CMDBuild.LABEL_WIDTH
		},

		initComponent: function() {
			this.emailContentField = Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
				name: CMDBuild.core.proxy.CMProxyConstants.BODY,
				hideLabel: true,
				value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.BODY),
				flex: 1,

				listeners: {
					scope: this,
					change: function() {
						this.delegate.cmOn('onEmailWindowFieldChange');
					}
				}
			});

			this.keepSynchronizationCheckbox = Ext.create('Ext.form.field.Checkbox', {
				fieldLabel: CMDBuild.Translation.keepSync,
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION,
				disabled: true,
				inputValue: true,
				uncheckedValue: false,
				value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
			});

			Ext.apply(this, {
				items: [
					this.keepSynchronizationCheckbox,
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.FROM,
						fieldLabel: CMDBuild.Translation.from,
						vtype: 'multiemail',
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.FROM),

						listeners: {
							scope: this,
							change: function() {
								this.delegate.cmOn('onEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.TO,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.to,
						vtype: 'multiemail',
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.TO),

						listeners: {
							scope: this,
							change: function() {
								this.delegate.cmOn('onEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CC,
						fieldLabel: CMDBuild.Translation.cc,
						vtype: 'multiemail',
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.CC),

						listeners: {
							scope: this,
							change: function() {
								this.delegate.cmOn('onEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.BCC,
						fieldLabel: CMDBuild.Translation.bcc,
						vtype: 'multiemail',
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.BCC),

						listeners: {
							scope: this,
							change: function() {
								this.delegate.cmOn('onEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.subject,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT),

						listeners: {
							scope: this,
							change: function() {
								this.delegate.cmOn('onEmailWindowFieldChange');
							}
						}
					},
					this.emailContentField
				]
			});

			this.callParent(arguments);

			this.fixIEFocusIssue();
		},

		/**
		 * Sometimes on IE the HtmlEditor is not able to take the focus after the mouse click. With this call it works. The reason is currently unknown.
		 */
		fixIEFocusIssue: function() {
			if (Ext.isIE)
				this.mon(this.emailContentField, 'render', function() {
					try {
						this.emailContentField.focus();
					} catch (e) {}
				}, this);
		}
	});

})();