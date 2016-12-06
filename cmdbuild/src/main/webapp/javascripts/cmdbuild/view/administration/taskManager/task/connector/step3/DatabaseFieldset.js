(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.connector.step3.DatabaseFieldset', {
		extend: 'Ext.form.FieldSet',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Connector'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Step3}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldDatabaseInstanceName: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldDatabaseType: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldDatabaseAddress: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldDatabaseName: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldDatabasePassword: undefined,

		/**
		 * @property {Ext.form.field.Number}
		 */
		fieldDatabasePort: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		fieldDatabaseUsername: undefined,

		checkboxName: CMDBuild.core.constants.Proxy.DATASOURCE_TYPE,
		checkboxValue: 'db',
		checkboxToggle: true,
		collapsed: true,
		collapsible: true,
		title: CMDBuild.Translation.datasourceRelationalDatabase,
		toggleOnTitleClick: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.fieldDatabaseType = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						valueField: CMDBuild.core.constants.Proxy.KEY,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.administration.taskManager.task.Connector.getStoreDbTypes(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function (field, records, options) {
								this.delegate.cmfg('onTaskManagerFormTaskConnectorStep3DatabaseTypeSelect');
							}
						}
					}),
					this.fieldDatabaseAddress = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_ADDRESS,
						fieldLabel: CMDBuild.Translation.address,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					}),
					this.fieldDatabasePort = Ext.create('Ext.form.field.Number', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_PORT,
						fieldLabel: CMDBuild.Translation.port,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
						minValue: 1,
						maxValue: 65535,
					}),
					this.fieldDatabaseName = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_NAME,
						fieldLabel: CMDBuild.Translation.databaseName,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					}),
					this.fieldDatabaseInstanceName = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_INSATANCE_NAME,
						fieldLabel: CMDBuild.Translation.instance,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
						forceDisabled: true
					}),
					this.fieldDatabaseUsername = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_USERNAME,
						fieldLabel: CMDBuild.Translation.username,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					}),
					this.fieldDatabasePassword = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_PASSWORD,
						inputType: 'password',
						fieldLabel: CMDBuild.Translation.password,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DATASOURCE_TABLE_VIEW_PREFIX,
						fieldLabel: CMDBuild.Translation.entityFilter,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			expand: function (field, eOpts) {
				this.delegate.cmfg('onTaskManagerFormTaskConnectorStep3FieldsetDatabaseExpand');
			}
		}
	});

})();
