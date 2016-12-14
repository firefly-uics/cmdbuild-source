(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.common.field.cron.Cron', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormCommonFieldCronAdvancedFieldChange',
			'onTaskManagerFormCommonFieldCronAdvancedRadioCheck',
			'onTaskManagerFormCommonFieldCronBaseComboSelect',
			'onTaskManagerFormCommonFieldCronBaseRadioCheck',
			'onTaskManagerFormCommonFieldCronShow',
			'taskManagerFormCommonFieldCronIsValid',
			'taskManagerFormCommonFieldCronValueGet',
			'taskManagerFormCommonFieldCronReset',
			'taskManagerFormCommonFieldCronValueSet'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.cron.CronView}
		 */
		view: undefined,

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		panelAdvancedValueSet: function (value) {
			if (Ext.isString(value) && !Ext.isEmpty(value)) {
				value = value.split(' ');

				this.view.fieldMinute.setValue(value[0]);
				this.view.fieldHour.setValue(value[1]);
				this.view.fieldDayOfMonth.setValue(value[2]);
				this.view.fieldMonth.setValue(value[3]);
				this.view.fieldDayOfWeek.setValue(value[4]);
			}
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		panelAdvancedSetDisabled: function (state) {
			state = Ext.isBoolean(state) ? state : true;

			this.view.fieldMinute.setDisabled(state);
			this.view.fieldHour.setDisabled(state);
			this.view.fieldDayOfMonth.setDisabled(state);
			this.view.fieldMonth.setDisabled(state);
			this.view.fieldDayOfWeek.setDisabled(state);
		},

		/**
		 * @return {String} cronExpression
		 *
		 * @private
		 */
		getCronExpression: function () {
			var cronexpression = Ext.String.trim(this.view.fieldMinute.getValue())
				+ ' ' + Ext.String.trim(this.view.fieldHour.getValue())
				+ ' ' + Ext.String.trim(this.view.fieldDayOfMonth.getValue())
				+ ' ' + Ext.String.trim(this.view.fieldMonth.getValue())
				+ ' ' + Ext.String.trim(this.view.fieldDayOfWeek.getValue());

			return Ext.String.trim(cronexpression);
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormCommonFieldCronAdvancedFieldChange: function () {
			var cronExpression = this.getCronExpression();

			// Error handling
				if (!Ext.isString(cronExpression) || Ext.isEmpty(cronExpression))
					return _error('onTaskManagerFormCommonFieldCronAdvancedFieldChange(): unmanaged cronExpression', this, cronExpression);
			// END: Error handling

			this.view.fieldBase.setValue(
				this.view.fieldBase.getStore().find(CMDBuild.core.constants.Proxy.VALUE, cronExpression) < 0 ? null : cronExpression
			);
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormCommonFieldCronAdvancedRadioCheck: function () {
			// Advanced panel enable
			this.panelAdvancedSetDisabled(false);

			// Base panel disable
			this.view.fieldBase.disable();
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormCommonFieldCronBaseComboSelect: function () {
			this.panelAdvancedValueSet(this.view.fieldBase.getValue());
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormCommonFieldCronBaseRadioCheck: function () {
			// Advanced panel disable
			this.panelAdvancedSetDisabled(true);

			// Base panel enable
			this.view.fieldBase.enable();
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormCommonFieldCronShow: function () {
			this.view.fieldRadioBase.setValue(true);

			if (Ext.isEmpty(this.view.fieldBase.getValue()) && !Ext.isEmpty(this.getCronExpression()))
				this.view.fieldRadioAdvanced.setValue(true);
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Boolean}
		 */
		taskManagerFormCommonFieldCronIsValid: function () {
			return (
				this.view.fieldMinute.isValid()
				& this.view.fieldHour.isValid()
				& this.view.fieldDayOfMonth.isValid()
				& this.view.fieldMonth.isValid()
				& this.view.fieldDayOfWeek.isValid()
			);
		},

		/**
		 * @returns {Void}
		 */
		taskManagerFormCommonFieldCronValueGet: function () {
			return this.getCronExpression();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerFormCommonFieldCronReset: function () {
			// Base field reset
			this.view.fieldRadioBase.reset();
			this.view.fieldBase.reset();

			// Advanced fields reset
			this.view.fieldRadioAdvanced.reset();
			this.view.fieldMinute.reset();
			this.view.fieldHour.reset();
			this.view.fieldDayOfMonth.reset();
			this.view.fieldMonth.reset();
			this.view.fieldDayOfWeek.reset();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerFormCommonFieldCronValueSet: function (value) {
			this.panelAdvancedValueSet(value);
		}
	});

})();
