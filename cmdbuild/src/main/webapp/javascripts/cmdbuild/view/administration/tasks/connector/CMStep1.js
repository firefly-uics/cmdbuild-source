(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep1Delegate', {
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

		getValueId: function() {
			return this.view.idField.getValue();
		},

		setDisabledTypeField: function(state) {
			this.view.typeField.setDisabled(state);
		},

		setValueActive: function(value) {
			this.view.activeField.setValue(value);
		},

		setValueDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		setValueId: function(value) {
			this.view.idField.setValue(value);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.administration.tasks.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				value: tr.tasksTypes.connector,
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.ServiceProxy.parameter.ACTIVE,
				fieldLabel: CMDBuild.Translation.administration.tasks.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.operationsCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'CMDBuild.ServiceProxy.parameter.TO_SYNCHRONIZE',
				fieldLabel: 'tr.taskConnector.toSynchronize',
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getConnectorOperations(),
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			// SendMail configuration
				this.emailTemplateCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					fieldLabel: tr.template,
					labelWidth: CMDBuild.LABEL_WIDTH,
					itemId: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					store: CMDBuild.core.proxy.CMProxyEmailTemplates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.sendMailFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.sendMail + ' --> DA CONFERMARE',
					checkboxToggle: true,
					collapsed: true,

					layout: {
						type: 'vbox'
					},

					items: [this.emailTemplateCombo]
				});

				this.sendMailFieldset.fieldWidthsFix();
			// END: SendMail configuration

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.operationsCombo,
					this.sendMailFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();