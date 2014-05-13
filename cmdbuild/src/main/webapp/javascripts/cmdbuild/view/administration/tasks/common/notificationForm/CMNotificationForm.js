(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
		extend: 'Ext.form.FieldContainer',

		border: false,
		width: '100%',

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param (Object) configuration
		 * @param (Object) configuration.sender
		 * @param (Object) configuration.template
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController', this);

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.sender)) {
				this.senderComboConfig = { delegate: this.delegate };
			} else {
				this.senderComboConfig = configuration.sender;
				this.senderComboConfig.delegate = this.delegate;
			}

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.template)) {
				this.templateComboConfig = { delegate: this.delegate };
			} else {
				this.templateComboConfig = configuration.template;
				this.templateComboConfig.delegate = this.delegate;
			}

			// Default configuration disabled
			if (Ext.isEmpty(this.senderComboConfig.disabled)) {
				this.senderComboConfig.disabled = true;
			}

			// Default configuration disabled
			if (Ext.isEmpty(this.templateComboConfig.disabled)) {
				this.senderComboConfig.disabled = true;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			if (!this.senderComboConfig.disabled) {
				this.senderCombo = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormSenderCombo', this.senderComboConfig);
				this.delegate.senderCombo = this.senderCombo;
			}

			if (!this.templateComboConfig.disabled) {
				this.templateCombo = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormTemplateCombo', this.templateComboConfig);
				this.delegate.templateCombo = this.templateCombo;
			}

			Ext.apply(this, {
				items: [this.senderCombo, this.templateCombo]
			});

			this.callParent(arguments);
		}
	});

})();