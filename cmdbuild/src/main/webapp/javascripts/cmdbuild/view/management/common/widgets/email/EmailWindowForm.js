(function() {

	Ext.define('CMDBuild.view.management.common.widgets.email.EmailWindowForm', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		/**
		 * @property {Mixed}
		 */
		emailContentField: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.grid}
		 */
		record: undefined,

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
			labelAlign: 'right'
		},

		initComponent: function() {
			// EmailContentField setup
			if (this.readOnly) {
				this.emailContentField = Ext.create('Ext.panel.Panel', {
					frame: true,
					border: true,
					html: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT),
					autoScroll: true,
					flex: 1
				});
			} else {
				this.emailContentField = Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
					name: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
					hideLabel: true,
					value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT),
					flex: 1
				});
			}

			Ext.apply(this, {
				items: [
					{
						xtype: 'hidden',
						name: CMDBuild.core.proxy.CMProxyConstants.ID,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID)
					},
					{
						xtype: 'hidden',
						name: CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY)
					},
					{
						xtype: 'hidden',
						name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
						disabled: this.readOnly,
						vtype: this.readOnly ? null : 'multiemail',
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS)
					},
					{
						xtype: this.readOnly ? 'displayfield' : 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
						disabled: this.readOnly,
						vtype: this.readOnly ? null : 'multiemail',
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES)
					},
					{
						xtype: this.readOnly ? 'displayfield' : 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
						disabled: this.readOnly,
						vtype: this.readOnly ? null : 'multiemail',
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES)
					},
					{
						xtype: this.readOnly ? 'displayfield' : 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
						disabled: this.readOnly,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT)
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