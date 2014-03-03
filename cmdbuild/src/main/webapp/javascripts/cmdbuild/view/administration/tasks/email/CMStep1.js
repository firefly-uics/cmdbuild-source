(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1Delegate', {

		delegate: undefined,
		filterWindow: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				// FilterWindow events
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

		onFromAddressFilterButtonClick: function() {
			var me = this;

			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.email.CMFilterWindow', {
				title: tr.fromAddressFilter,
				type: 'FromAddress',
				content: me.view.getForm().findField('FromAddresFilterField').getValue(),
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

			this.view.getForm().findField('FromAddresFilterField').setValue(filterString);
		},

		onFromAddressFilterWindowAbort: function() {
			// TODO: Fix reverting edits to store data
			this.view.getForm().findField('FromAddresFilterField').reset();
			this.filterWindow.hide();
		},

		onSubjectFilterButtonClick: function() {
			var me = this;

			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.email.CMFilterWindow', {
				title: tr.onSubjectFilter,
				type: 'Subject',
				content: me.view.getForm().findField('SubjectFilterField').getValue(),
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

			this.view.getForm().findField('SubjectFilterField').setValue(filterString);
		},

		onSubjectFilterWindowAbort: function() {
			// TODO: Fix reverting edits to store data
			this.view.getForm().findField('SubjectFilterField').reset();
			this.filterWindow.hide();
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep1Delegate');
			this.delegate.view = this;

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

			this.items = [
				this.typeField,
				this.idField,
				{
					xtype: 'combo',
					fieldLabel: tr.emailAccount,
					name: CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT,
					store: CMDBuild.core.serviceProxy.CMProxyConfigurationEmailAccounts.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				},
				{
					xtype: 'numberfield',
					fieldLabel: tr.pollingFrequency,
					minValue: 1,
					name: CMDBuild.ServiceProxy.parameter.POLLING_FREQUENCY,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				},
				{
					xtype: 'container',
					layout: 'hbox',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH
					},

					items: [
						{
							xtype: 'textareafield',
							id: 'FromAddresFilterField',
							fieldLabel: tr.fromAddressFilter,
							name: CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS,
							readOnly: true,
							width: CMDBuild.CFG_BIG_FIELD_WIDTH
						},
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
				},
				{
					xtype: 'container',
					layout: 'hbox',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH
					},

					items: [
						{
							xtype: 'textareafield',
							id: 'SubjectFilterField',
							fieldLabel: tr.onSubjectFilter,
							name: CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT,
							readOnly: true,
							width: CMDBuild.CFG_BIG_FIELD_WIDTH,
						},
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
				},
				{
					xtype: 'checkbox',
					name: 'isActive',
					fieldLabel: '@@ Run on save',
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				}
			];

			this.callParent(arguments);
		}
	});

})();