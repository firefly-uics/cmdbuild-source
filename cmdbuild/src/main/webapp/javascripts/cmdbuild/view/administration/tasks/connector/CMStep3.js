(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep3Delegate', {
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
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onDbFieldsetExpand':
					return this.onDbFieldsetExpand();

				case 'onLdapFieldsetExpand':
					return this.onLdapFieldsetExpand();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			getTypeDataSource: function() {
				if (this.view.dbFieldset.checkboxCmp.getValue())
					return 'db';

				if (this.view.ldapFieldset.checkboxCmp.getValue())
					return 'ldap';

				return false;
			},

		onDbFieldsetExpand: function() {
			this.view.ldapFieldset.collapse();
			this.view.ldapFieldset.reset();
		},

		onLdapFieldsetExpand: function() {
			this.view.dbFieldset.collapse();
			this.view.dbFieldset.reset();
		}

		// SETters functions
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep3Delegate', this);

			// DataSource: relationa databases configuration
				this.dbType = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_DB_TYPE,
					fieldLabel: CMDBuild.Translation.administration.tasks.type,
					labelWidth: CMDBuild.LABEL_WIDTH,
					store: CMDBuild.core.proxy.CMProxyTasks.getDbTypes(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.VALUE,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					forceSelection: true,
					editable: false
				});

				this.dbAddressField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_ADDRESS,
					fieldLabel: CMDBuild.Translation.address,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbPortField = Ext.create('Ext.form.field.Number', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_DB_PORT,
					fieldLabel: CMDBuild.Translation.port,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					minValue: 1,
					maxValue: 65535,
					allowBlank: true
				});

				this.dbNameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_DB_NAME,
					fieldLabel: tr.dbName,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbUsernameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_DB_USERNAME,
					fieldLabel: CMDBuild.Translation.username,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbPasswordField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_DB_PASSWORD,
					inputType: 'password',
					fieldLabel: CMDBuild.Translation.password,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.tableViewFilterField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.DATASOURCE_TABLE_VIEW_PREFIX,
					fieldLabel: tr.tableViewFilter,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.dbFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.dataSourceDbFieldset,
					checkboxToggle: true,
					collapsed: true,
					layout: 'vbox',

					items: [
						this.dbType,
						this.dbAddressField,
						this.dbPortField,
						this.dbNameField,
						this.dbUsernameField,
						this.dbPasswordField,
						this.tableViewFilterField
					],

					listeners: {
						beforeexpand: function(fieldset, eOpts) {
							me.delegate.cmOn('onDbFieldsetExpand');
						}
					}
				});

				this.dbFieldset.fieldWidthsFix();
			// END - DataSource: relationa databases configuration

			// DataSource: LDAP configuration
				this.ldapAddressField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.ADDRESS,
					fieldLabel: CMDBuild.Translation.address,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.ldapUsernameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.USERNAME,
					fieldLabel: CMDBuild.Translation.username,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.ldapPasswordField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.PASSWORD,
					inputType: 'password',
					fieldLabel: CMDBuild.Translation.password,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.ldapFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.dataSourceLdapFieldset,
					checkboxToggle: true,
					collapsed: true,
					layout: 'vbox',

					items: [
						this.ldapAddressField,
						this.ldapUsernameField,
						this.ldapPasswordField
					],

					listeners: {
						beforeexpand: function(fieldset, eOpts) {
							me.delegate.cmOn('onLdapFieldsetExpand');
						}
					}
				});

				this.ldapFieldset.fieldWidthsFix();
			// END - DataSource: LDAP configuration

			Ext.apply(this, {
				items: [
					this.dbFieldset
// TODO: future implementation
//					,
//					this.ldapFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();