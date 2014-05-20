(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		filterWindow: undefined,
		parentDelegate: undefined,
		taskType: 'email',
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

		// GETters functions
			/**
			 * @return (Object) delegate
			 */
			getFromAddressFilterDelegate: function() {
				return this.view.fromAddresFilter.delegate;
			},

			/**
			 * @return (Object) delegate
			 */
			getSubjectFilterDelegate: function() {
				return this.view.subjectFilter.delegate;
			},

			/**
			 * @return (String)
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

		// SETters functions
			/**
			 * @param (Boolean) state
			 */
			setAllowBlankEmailAccountCombo: function(state) {
				this.view.emailAccountCombo.allowBlank = state;
			},

			/**
			 * @param (Boolean) state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param (String) value
			 */
			setValueActive: function(value) {
				this.view.activeField.setValue(value);
			},

			/**
			 * @param (String) value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param (String) value
			 */
			setValueEmailAccount: function(value) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.view.emailAccountCombo.forceSelection = false;
				this.view.emailAccountCombo.setValue(value);
				this.view.emailAccountCombo.forceSelection = true;
			},

			/**
			 * @param (String) value
			 */
			setValueFilterFromAddress: function(value) {
				this.getFromAddressFilterDelegate().setValue(value);
			},

			/**
			 * @param (String) value
			 */
			setValueFilterSubject: function(value) {
				this.getSubjectFilterDelegate().setValue(value);
			},

			/**
			 * @param (String) value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.administration.tasks.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				value: tr.tasksTypes.email,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				submitValue: false
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

			this.emailAccountCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT,
				fieldLabel: tr.taskEmail.emailAccount,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyEmailAccounts.getStore(),
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			this.fromAddresFilter = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
				fieldContainer: {
					fieldLabel: tr.taskEmail.fromAddressFilter
				},
				textarea: {
					name: CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS,
					id: 'FromAddresFilterField'
				},
				button: {
					titleWindow: tr.taskEmail.fromAddressFilter
				}
			});

			this.subjectFilter = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
				fieldContainer: {
					fieldLabel: tr.taskEmail.subjectFilter
				},
				textarea: {
					name: CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT,
					id: 'SubjectFilterField'
				},
				button: {
					titleWindow: tr.taskEmail.subjectFilter
				}
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.emailAccountCombo,
					this.fromAddresFilter,
					this.subjectFilter
				]
			});

			this.callParent(arguments);
		}
	});

})();