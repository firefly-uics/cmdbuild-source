(function () {

	Ext.require([
		'CMDBuild.core.constants.FieldWidths',
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.proxy.administration.taskManager.task.common.field.Report'
	]);

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.report.ReportView', {
		extend: 'Ext.form.FieldContainer',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.report.Report}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		combo: undefined,

		/**
		 * @cfg {Object}
		 */
		config: {},

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		extension: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.report.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		submitValue: false,

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
			this.delegate = Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.report.Report', { view: this });

			Ext.apply(this, {
				items: [
					this.combo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.REPORT_NAME,
						fieldLabel: CMDBuild.Translation.report,
						labelWidth: this.config.labelWidth,
						valueField: CMDBuild.core.constants.Proxy.TITLE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.administration.taskManager.task.common.field.Report.getStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) { // Selected by setValue()
								field.getStore().on('load', function (store, records, successful, eOpts) {
									this.delegate.cmfg('onTaskManagerCommonFieldReportComboSelect', {
										merge: true,
										record: store.findRecord(CMDBuild.core.constants.Proxy.TITLE, newValue)
									});
								}, this, { single: true });
							},
							select: function (field, records, eOpts) { // Selected by user
								this.delegate.cmfg('onTaskManagerCommonFieldReportComboSelect', { record: records[0] });
							}
						}
					}),
					this.extension = Ext.create('Ext.form.field.ComboBox', { // Prepared for future implementations
						name: CMDBuild.core.constants.Proxy.REPORT_EXTENSION,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: this.config.labelWidth,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
						forceSelection: true,
						editable: false,

						value: CMDBuild.core.constants.Proxy.PDF, // Default value

						store: CMDBuild.proxy.administration.taskManager.task.common.field.Report.getStoreExtension(),
						queryMode: 'local'
					}),
					this.grid = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.report.GridPanel', {
						delegate: this.delegate,
						margin: '0 0 0 ' + (this.config.labelWidth + 5),
						name: CMDBuild.core.constants.Proxy.REPORT_PARAMETERS
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 */
		disable: function () {
			this.delegate.cmfg('taskManagerCommonFieldReportDisable');
		},

		/**
		 * @returns {Void}
		 */
		enable: function () {
			this.delegate.cmfg('taskManagerCommonFieldReportEnable');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('taskManagerCommonFieldReportIsValid');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerCommonFieldReportReset');
		}
	});

})();
