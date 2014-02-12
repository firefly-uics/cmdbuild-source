(function() {

	// FAKE DATAS
	var taskTypes = Ext.create('Ext.data.Store', {
		fields: ['abbr', 'name'],
		data: [
			{ 'abbr': '', 'name': '' },
			{ 'abbr': 'mail', 'name': 'Mail' },
			{ 'abbr': 'event', 'name': 'Event' }
		]
	});

	var imaps = Ext.create('Ext.data.Store', {
			fields: ['id', 'name'],
			data : [
				{'id': '1', 'name': 'imap.gmail.com' },
				{'id': '2', 'name': 'imap.googlemail.com' },
				{'id': '3', 'name': 'imap.secureserver.org' }
			]
	});
	// END FAKE DATAS

	Ext.define("CMDBuild.view.administration.tasks.email.CMStep1Delegate", {
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.filterWindow = null;
		},

		cmOn: function(name, param, callBack) {
			var me = this;

			switch (name) {

				// FilterWindow events
				case 'onFromAddressFilterButtonClick': {
					this.filterWindow = new CMDBuild.view.administration.tasks.email.CMFilterWindow({
						title: '@@ Filter on FromAddress',
						type: 'Address',
						content: me.view.getForm().findField('FromAddresFilterField').getValue(),
					});
					this.filterWindow.delegate.parentDelegate = this;
					this.filterWindow.show();
				} break;

				case 'onSubjectFilterButtonClick': {
					this.filterWindow = new CMDBuild.view.administration.tasks.email.CMFilterWindow({
						title: '@@ Filter on Subject',
						type: 'Subject',
						content: me.view.getForm().findField('SubjectFilterField').getValue(),
					});
					this.filterWindow.delegate.parentDelegate = this;
					this.filterWindow.show();
				} break;

				case 'onAddressFilterChange': {
					var filterString = '';

					for (key in param) {
						if (param[key] !== '') {
							if (filterString != '')
								filterString = filterString + ' OR ';

							filterString = filterString.concat(param[key]);
						}
					}

					me.view.getForm().findField('FromAddresFilterField').setValue(filterString);
				} break;

				case 'onSubjectFilterChange': {
					var filterString = '';

					for (key in param) {
						if (param[key] !== '') {
							if (filterString != '')
								filterString = filterString + ' OR ';

							filterString = filterString.concat(param[key]);
						}
					}

					me.view.getForm().findField('SubjectFilterField').setValue(filterString);
				} break;

				case 'onFilterWindowConfirm': {
					this.filterWindow.hide();
				} break;

				case 'onAddressFilterWindowAbort': { // TODO: Fix reverting edits to store datas
					me.view.getForm().findField('FromAddresFilterField').setValue();
					this.filterWindow.hide();
				} break;

				case 'onSubjectFilterWindowAbort': { // TODO: Fix reverting edits to store datas
					me.view.getForm().findField('SubjectFilterField').setValue();
					this.filterWindow.hide();
				} break;

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.email.CMStep1", {
		extend: "Ext.form.Panel",

		delegate: undefined,

		border: false,
		bodyCls: 'cmgraypanel',
		height: '100%',
		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.items = [
				{
					fieldLabel: '@@ Type',
					name: 'type',
					value: 'email',
					disabled: true,
					cmImmutable: true,
					readOnly: true,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				},
				{
					fieldLabel: '@@ IMAP',
					name: 'imap',
					xtype: 'combo',
					store: imaps,
					queryMode: 'local', // Change in "remote" when server side will be implemented
					displayField: 'name',
					valueField: 'id',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				},
				{
					xtype: 'numberfield',
					fieldLabel: '@@ Polling frequency (minutes)',
					minValue: 1,
					name: 'stepTime',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
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
							fieldLabel: '@@ From address filter',
							name: 'fromAddressFilter',
							id: 'FromAddresFilterField',
							readOnly: true,
							itemId: 'fromAddressFilter',
							width: CMDBuild.ADM_BIG_FIELD_WIDTH
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
							id: 'SubjectFilterField',
							fieldLabel: '@@ Subject filter',
							name: 'subjectFilter',
							xtype: 'textareafield',
							readOnly: true,
							itemId: 'subjectFilter',
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
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
				}
			];

			this.delegate = new CMDBuild.view.administration.tasks.email.CMStep1Delegate(this);

			this.callParent(arguments);
		},

//		disableTypeField: function() {
//			this.getForm().findField('type').disable();
//		}
	});

})();