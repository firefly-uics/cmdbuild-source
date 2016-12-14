(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step3', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Asynchronous}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEventAsynchronousStep3Show',
			'onTaskManagerFormTaskEventAsynchronousStep3ValidateSetup = onTaskManagerFormTaskEventAsynchronousValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.event.asynchronous.Step3View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Asynchronous} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.event.asynchronous.Step3View', { delegate: this });
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousStep3Show: function () {
			this.view.fieldCronExpression.fireEvent('show');
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousStep3ValidateSetup: function (fullValidation) {
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
