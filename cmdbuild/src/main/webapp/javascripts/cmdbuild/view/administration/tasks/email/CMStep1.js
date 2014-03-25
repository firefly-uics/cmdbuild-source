(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		filterWindow: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) { // FilterWindow events
				case 'onFromAddressFilterButtonClick':
					return this.onFromAddressFilterButtonClick();

				case 'onFromAddressFilterChange':
					return this.onFromAddressFilterChange(param);

				case 'onFromAddressFilterWindowAbort':
					return this.onFromAddressFilterWindowAbort();

				case 'onSubjectFilterChange':
					return this.onSubjectFilterChange(param);

				case 'onSubjectFilterButtonClick':
					return this.onSubjectFilterButtonClick();

				case 'onSubjectFilterWindowAbort':
					return this.onSubjectFilterWindowAbort();

				case 'onFromAddressFilterWindowConfirm':
				case 'onSubjectFilterWindowConfirm':
					return this.filterWindow.hide();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		fillActive: function(value) {
			this.view.activeField.setValue(value);
		},

		fillDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		fillId: function(value) {
			this.view.idField.setValue(value);
		},

		fillEmailAccount: function(emailAccountName) {
			this.view.emailAccountCombo.setValue(emailAccountName);
		},

		fillFilterFromAddress: function(filterString) {
			this.view.fromAddresFilterField.setValue(filterString);
		},

		fillFilterSunbject: function(filterString) {
			this.view.subjectFilterField.setValue(filterString);
		},

		onFromAddressFilterButtonClick: function() {
			var me = this;

			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.email.CMFilterWindow', {
				title: tr.fromAddressFilter,
				type: 'FromAddress',
				content: me.view.fromAddresFilterField.getValue(),
			});

			this.filterWindow.delegate.parentDelegate = this;
			this.filterWindow.show();
		},

		onFromAddressFilterChange: function(parameters) {
			var filterString = '';

			for (key in parameters) {
				if (parameters[key] !== '') {
					if (filterString != '')
						filterString = filterString + ' OR ';

					filterString = filterString.concat(parameters[key]);
				}
			}

			this.view.fromAddresFilterField.setValue(filterString);
		},

		onFromAddressFilterWindowAbort: function() {
			// TODO: Fix reverting edits to store data
			this.view.fromAddresFilterField.reset();
			this.filterWindow.hide();
		},

		onSubjectFilterButtonClick: function() {
			var me = this;

			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.email.CMFilterWindow', {
				title: tr.onSubjectFilter,
				type: 'Subject',
				content: me.view.subjectFilterField.getValue(),
			});

			this.filterWindow.delegate.parentDelegate = this;
			this.filterWindow.show();
		},

		onSubjectFilterChange: function(parameters) {
			var filterString = '';

			for (key in parameters) {
				if (parameters[key] !== '') {
					if (filterString != '')
						filterString = filterString + ' OR ';

					filterString = filterString.concat(parameters[key]);
				}
			}

			this.view.subjectFilterField.setValue(filterString);
		},

		onSubjectFilterWindowAbort: function() {
			// TODO: Fix reverting edits to store data
			this.view.subjectFilterField.reset();
			this.filterWindow.hide();
		},

		setDisabledTypeField: function(state) {
			this.view.typeField.setDisabled(state);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.administration.tasks.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				value: me.taskType,
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
				width: CMDBuild.ADM_BIG_FIELD_WIDTH
			});

			this.emailAccountCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT,
				fieldLabel: tr.emailAccount,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyEmailAccounts.getStore(),
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			// FromAddress filter configuration
				this.fromAddresFilterField = Ext.create('Ext.form.field.TextArea', {
					name: CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS,
					id: 'FromAddresFilterField',
					fieldLabel: tr.fromAddressFilter,
					labelWidth: CMDBuild.LABEL_WIDTH,
					readOnly: true,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.fromAddresFilterWrapper = Ext.create('Ext.container.Container', {
					layout: 'hbox',

					items: [
						this.fromAddresFilterField,
						{
							xtype: 'button',
							icon: 'images/icons/table.png',
							considerAsFieldToDisable: true,
							border: true,
							margin: 2,
							handler: function() {
								me.delegate.cmOn('onFromAddressFilterButtonClick');
							}
						}
					]
				});
			// END: FromAddress filter configuration

			// SubjectAddress filter configuration
				this.subjectFilterField = Ext.create('Ext.form.field.TextArea', {
					name: CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT,
					id: 'SubjectFilterField',
					fieldLabel: tr.onSubjectFilter,
					labelWidth: CMDBuild.LABEL_WIDTH,
					readOnly: true,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				});

				this.subjectFilterWrapper = Ext.create('Ext.container.Container', {
					layout: 'hbox',

					items: [
						this.subjectFilterField,
						{
							xtype: 'button',
							icon: 'images/icons/table.png',
							considerAsFieldToDisable: true,
							border: true,
							margin: 2,
							handler: function() {
								me.delegate.cmOn('onSubjectFilterButtonClick');
							}
						}
					]
				});
			// END: SubjectAddress filter configuration

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.emailAccountCombo,
					this.pollingFrequencyField,
					this.fromAddresFilterWrapper,
					this.subjectFilterWrapper
				]
			});

			this.callParent(arguments);
		}
	});

})();