(function() {

	Ext.define('CMDBuild.view.management.common.widgets.email.EmailWindowEditForm', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		/**
		 * @property {CMDBuild.view.common.field.CMHtmlEditorField}
		 */
		emailContentField: undefined,

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
			this.emailContentField = Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
				name: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
				hideLabel: true,
				value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT),
				flex: 1
			});

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
						vtype: 'multiemail',
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS)
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
						vtype: 'multiemail',
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES)
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
						vtype: 'multiemail',
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES)
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
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