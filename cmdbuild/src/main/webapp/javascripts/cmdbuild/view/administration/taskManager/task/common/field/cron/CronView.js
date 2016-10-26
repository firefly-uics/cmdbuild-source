(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.cron.CronView', {
		extend: 'Ext.form.FieldContainer',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.common.field.Cron'
		],

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.cron.Cron}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldBase: undefined,

		/**
		 * @property {CMDBuild.view.common.field.trigger.cron.Cron}
		 */
		fieldDayOfMonth: undefined,

		/**
		 * @property {CMDBuild.view.common.field.trigger.cron.Cron}
		 */
		fieldDayOfWeek: undefined,

		/**
		 * @property {CMDBuild.view.common.field.trigger.cron.Cron}
		 */
		fieldHour: undefined,

		/**
		 * @property {CMDBuild.view.common.field.trigger.cron.Cron}
		 */
		fieldMinute: undefined,

		/**
		 * @property {CMDBuild.view.common.field.trigger.cron.Cron}
		 */
		fieldMonth: undefined,

		/**
		 * @property {Ext.form.field.Radio}
		 */
		fieldRadioAdvanced: undefined,

		/**
		 * @property {Ext.form.field.Radio}
		 */
		fieldRadioBase: undefined,

		border: false,
		fieldLabel: CMDBuild.Translation.cronExpression,
		frame: false,
		hideLabel: true,

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
			this.delegate = Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.cron.Cron', { view: this });

			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						border: true,
						frame: true,
						layout: 'hbox',
						margin: '0 0 5 0',

						items: [
							this.fieldRadioBase = Ext.create('Ext.form.field.Radio', {
								name: CMDBuild.core.constants.Proxy.CRON_INPUT_TYPE,
								inputValue: CMDBuild.core.constants.Proxy.BASE,
								boxLabel: CMDBuild.Translation.basic,
								width: CMDBuild.core.constants.FieldWidths.LABEL,
								disablePanelFunctions: true,
								submitValue: false,

								listeners: {
									scope: this,
									change: function (field, newValue, oldValue, eOpts) {
										if (newValue)
											this.delegate.cmfg('onTaskManagerFormCommonFieldCronBaseRadioCheck');
									}
								}
							}),
							this.fieldBase = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.BASE,
								valueField: CMDBuild.core.constants.Proxy.VALUE,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								margins: '0 0 0 ' + (CMDBuild.core.constants.FieldWidths.LABEL - 45),
								disablePanelFunctions: true,
								forceSelection: true,
								editable: false,
								submitValue: false,

								store: CMDBuild.proxy.administration.taskManager.task.common.field.Cron.getStoreExpression(),
								queryMode: 'local',

								listeners: {
									scope: this,
									select: function (field, records, eOpts) {
										this.delegate.cmfg('onTaskManagerFormCommonFieldCronBaseComboSelect');
									}
								}
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						border: true,
						frame: true,
						layout: 'hbox',

						items: [
							this.fieldRadioAdvanced = Ext.create('Ext.form.field.Radio', {
								name: CMDBuild.core.constants.Proxy.CRON_INPUT_TYPE,
								inputValue: CMDBuild.core.constants.Proxy.ADVANCED,
								boxLabel: CMDBuild.Translation.advanced,
								width: CMDBuild.core.constants.FieldWidths.LABEL,
								disablePanelFunctions: true,
								submitValue: false,

								listeners: {
									scope: this,
									change: function (field, newValue, oldValue, eOpts) {
										if (newValue)
											this.delegate.cmfg('onTaskManagerFormCommonFieldCronAdvancedRadioCheck');
									}
								}
							}),
							{
								xtype: 'container',
								frame: false,
								border: false,

								items: [
									this.fieldMinute = Ext.create('CMDBuild.view.common.field.trigger.cron.Cron', {
										name: CMDBuild.core.constants.Proxy.MINUTE,
										fieldLabel: CMDBuild.Translation.minute,
										disablePanelFunctions: true,
										submitValue: false,

										listeners: {
											scope: this,
											change: function (field, newValue, oldValue) {
												this.delegate.cmfg('onTaskManagerFormCommonFieldCronAdvancedFieldChange');
											}
										}
									}),
									this.fieldHour = Ext.create('CMDBuild.view.common.field.trigger.cron.Cron', {
										name: CMDBuild.core.constants.Proxy.HOUR,
										fieldLabel: CMDBuild.Translation.hour,
										disablePanelFunctions: true,
										submitValue: false,

										listeners: {
											scope: this,
											change: function (field, newValue, oldValue) {
												this.delegate.cmfg('onTaskManagerFormCommonFieldCronAdvancedFieldChange');
											}
										}
									}),
									this.fieldDayOfMonth = Ext.create('CMDBuild.view.common.field.trigger.cron.Cron', {
										name: CMDBuild.core.constants.Proxy.DAY_OF_MONTH,
										fieldLabel: CMDBuild.Translation.dayOfMonth,
										disablePanelFunctions: true,
										submitValue: false,

										listeners: {
											scope: this,
											change: function (field, newValue, oldValue) {
												this.delegate.cmfg('onTaskManagerFormCommonFieldCronAdvancedFieldChange');
											}
										}
									}),
									this.fieldMonth = Ext.create('CMDBuild.view.common.field.trigger.cron.Cron', {
										name: CMDBuild.core.constants.Proxy.MONTH,
										fieldLabel: CMDBuild.Translation.month,
										disablePanelFunctions: true,
										submitValue: false,

										listeners: {
											scope: this,
											change: function (field, newValue, oldValue) {
												this.delegate.cmfg('onTaskManagerFormCommonFieldCronAdvancedFieldChange');
											}
										}
									}),
									this.fieldDayOfWeek = Ext.create('CMDBuild.view.common.field.trigger.cron.Cron', {
										name: CMDBuild.core.constants.Proxy.DAY_OF_WEEK,
										fieldLabel: CMDBuild.Translation.dayOfWeek,
										disablePanelFunctions: true,
										submitValue: false,

										listeners: {
											scope: this,
											change: function (field, newValue, oldValue) {
												this.delegate.cmfg('onTaskManagerFormCommonFieldCronAdvancedFieldChange');
											}
										}
									})
								]
							}
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (view, eOpts) {
				this.delegate.cmfg('onTaskManagerFormCommonFieldCronShow');
			}
		},

		/**
		 * @param {String} mode
		 *
		 * @returns {String or Object}
		 */
		getValue: function (mode) {
			return this.delegate.cmfg('taskManagerFormCommonFieldCronValueGet', mode);
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('taskManagerFormCommonFieldCronIsValid');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerFormCommonFieldCronReset');
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			return this.delegate.cmfg('taskManagerFormCommonFieldCronValueSet', value);
		}
	});

})();
