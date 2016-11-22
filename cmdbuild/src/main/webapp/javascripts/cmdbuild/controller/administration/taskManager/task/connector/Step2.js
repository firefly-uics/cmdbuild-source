(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.connector.Step2', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Connector}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskConnectorStep2Show',
			'onTaskManagerFormTaskConnectorStep2ValidateSetup = onTaskManagerFormTaskConnectorValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.connector.Step2View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.connector.Connector} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.connector.Step2View', { delegate: this });
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep2Show: function () {
			this.view.fieldCronExpression.fireEvent('show');
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep2ValidateSetup: function (fullValidation) {
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
