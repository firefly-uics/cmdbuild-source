(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		checkWorkflowComboSelected: function() {
			if (this.getValueWorkflowCombo())
				return true;

			return false;
		},

		// GETters functions
			getWorkflowDelegate: function() {
				return this.view.workflowForm.delegate;
			},

			getValueWorkflowAttributeGrid: function() {
				return this.getWorkflowDelegate().getValueGrid();
			},

			getValueWorkflowCombo: function() {
				return this.getWorkflowDelegate().getValueCombo();
			},

			getValueWorkflowFieldsetCheckbox: function() {
				return this.view.workflowFieldset.checkboxCmp.getValue();
			},

			getValueNotificationFieldsetCheckbox: function() {
				return this.view.notificationFieldset.checkboxCmp.getValue();
			},

		// SETters functions
			setDisabledWorkflowAttributesGrid: function(state) {
				this.getWorkflowDelegate().setDisabledAttributesGrid(state);
			},

			/**
			 * @param (Boolean) value
			 */
			setValueNotificationFieldsetCheckbox: function(value) {
				if (value) {
					this.view.notificationFieldset.expand();
				} else {
					this.view.notificationFieldset.collapse();
				}
			},

			setValueNotificationEmailAccount: function(value) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.view.notificationEmailAccountCombo.forceSelection = false;
				this.view.notificationEmailAccountCombo.setValue(value);
				this.view.notificationEmailAccountCombo.forceSelection = true;
			},

			setValueNotificationEmailTemplate: function(value) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.view.notificationEmailTemplateCombo.forceSelection = false;
				this.view.notificationEmailTemplateCombo.setValue(value);
				this.view.notificationEmailTemplateCombo.forceSelection = true;
			},

			setValueWorkflowAttributesGrid: function(value) {
				this.getWorkflowDelegate().setValueGrid(value);
			},

			setValueWorkflowAttributesGrid: function(value) {
				this.getWorkflowDelegate().setValueGrid(value);
			},

			setValueWorkflowCombo: function(value) {
				this.getWorkflowDelegate().setValueCombo(value);
			},

			/**
			 * @param (Boolean) value
			 */
			setValueWorkflowFieldsetCheckbox: function(value) {
				if (value) {
					this.view.workflowFieldset.expand();
				} else {
					this.view.workflowFieldset.collapse();
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep3Delegate', this);

			// Email notification configuration
				this.notificationEmailAccountCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_ACCOUNT,
					fieldLabel: tr.taskEmail.emailAccount,
					labelWidth: CMDBuild.LABEL_WIDTH,
					store: CMDBuild.core.proxy.CMProxyEmailAccounts.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					forceSelection: true,
					editable: false
				});

				this.notificationEmailTemplateCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE,
					fieldLabel: tr.template,
					labelWidth: CMDBuild.LABEL_WIDTH,
					store: CMDBuild.core.proxy.CMProxyEmailTemplates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.sendMail,
					checkboxToggle: true,
					checkboxName: CMDBuild.ServiceProxy.parameter.NOTIFICATION_ACTIVE,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.notificationEmailAccountCombo, this.notificationEmailTemplateCombo]
				});
			// END: Email notification configuration

			// Workflow configuration
				this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
					combo: {
						name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME
					}
				});

				this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.startWorkflow,
					checkboxToggle: true,
					collapsed: true,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [this.workflowForm]
				});
			// END: Workflow configuration

			Ext.apply(this, {
				items: [
					this.notificationFieldset,
					this.workflowFieldset
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable attribute table to correct malfunction that enables on class select
			 */
			show: function(view, eOpts) {
				if (!this.delegate.checkWorkflowComboSelected())
					this.delegate.setDisabledWorkflowAttributesGrid(true);
			}
		}
	});

})();