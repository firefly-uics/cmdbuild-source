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

				case 'onAddressFilterWindowAbort': {
					me.view.getForm().findField('FromAddresFilterField').setValue();
					this.filterWindow.hide();
				} break;

				case 'onSubjectFilterWindowAbort': {
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

		defaultType: 'textfield',
		border: false,
		bodyCls: 'cmgraypanel',
		height: '100%',

		defaults: {
			anchor: '100%'
		},

		initComponent: function() {
			var me = this;

			this.items = [
				{
					fieldLabel: '@@ IMAP',
					name: 'imap',
					xtype: 'combo',
					store: imaps,
					queryMode: 'local', // Change in "remote" when server side will be implemented
					displayField: 'name',
					valueField: 'id'
				},
				{
					xtype: 'numberfield',
					minValue: 1,
					fieldLabel: '@@ Polling frequency (minutes)',
					name: 'stepTime'
				},
				{
					xtype: 'container',
					layout: 'hbox',
					items: [
						{
							id: 'FromAddresFilterField',
							fieldLabel: '@@ From address filter',
							name: 'fromAddressFilter',
							xtype: 'textareafield',
							readOnly: true,
							itemId: 'fromAddressFilter'
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
					items: [
						{
							id: 'SubjectFilterField',
							fieldLabel: '@@ Subject filter',
							name: 'subjectFilter',
							xtype: 'textareafield',
							readOnly: true,
							itemId: 'subjectFilter'
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
		}
	});

})();