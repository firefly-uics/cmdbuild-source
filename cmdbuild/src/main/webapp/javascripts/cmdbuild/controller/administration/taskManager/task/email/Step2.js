(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.email.Step2', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEmailStep2Show',
			'onTaskManagerFormTaskEmailStep2ValidateSetup = onTaskManagerFormTaskEmailValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.email.Step2View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.email.Email} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.email.Step2View', { delegate: this });
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep2Show: function () {
			this.view.fieldCronExpression.fireEvent('show');
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep2ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			// Enable advanced
			this.view.fieldCronExpression.fieldRadioAdvanced.setValue(fullValidation);

			// Setup advanced fields
			this.view.fieldCronExpression.fieldMinute.allowBlank = !fullValidation;
			this.view.fieldCronExpression.fieldHour.allowBlank = !fullValidation;
			this.view.fieldCronExpression.fieldDayOfMonth.allowBlank = !fullValidation;
			this.view.fieldCronExpression.fieldMonth.allowBlank = !fullValidation;
			this.view.fieldCronExpression.fieldDayOfWeek.allowBlank = !fullValidation;
		}
	});

})();
